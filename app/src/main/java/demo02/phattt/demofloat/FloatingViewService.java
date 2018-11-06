package demo02.phattt.demofloat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FloatingViewService extends Service {
    private WindowManager wm;
    private View FloatingView;
    private MediaProjection mediaProjection;
    private ArrayList<String> detectedText = null;

    public FloatingViewService(){}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getIntExtra("code", 0) == 2){
            detectedText = intent.getStringArrayListExtra("Texts");
            String text = "";
            if (detectedText != null) {
                for (String t : detectedText) {
                    text += t + "\n";
                }
                Toast.makeText(FloatingViewService.this, text, Toast.LENGTH_SHORT).show();
            }
        } else {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        int resultCode = intent.getIntExtra("resultCode", 0);
        Intent data = (Intent) intent.getParcelableExtra("data");
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);}
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.addView(FloatingView, params);

        final View collapsedView = FloatingView.findViewById(R.id.collapse_view);
        final View expandedView = FloatingView.findViewById(R.id.expanded_container);

        ImageView collapsedCloseBtn = (ImageView) FloatingView.findViewById(R.id.BtnClose);
        collapsedCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        ImageView BtnCamera = (ImageView) FloatingView.findViewById(R.id.BtnCamera);
        BtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot();

                /////////////////////////////////////////
            }
        });



        ImageView expandedCloseBtn = (ImageView) FloatingView.findViewById(R.id.btnClose);
        expandedCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });

        ImageView btnOpen = (ImageView) FloatingView.findViewById(R.id.btnOpen);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                stopSelf();
            }
        });

        FloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        int X = (int) (event.getRawX() - initialTouchX);
                        int Y = (int) (event.getRawY() - initialTouchY);

                        if (X < 10 && Y < 10){
                            if (isViewCollapsed()){
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        wm.updateViewLayout(FloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void takeScreenshot(){
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getRealSize(size);
        display.getMetrics(metrics);
        int width = size.x;
        final int height = size.y;
        int dpi = metrics.densityDpi;
        ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
        mediaProjection.createVirtualDisplay("Screenshot", width, height, dpi, flags,
                imageReader.getSurface(), null, null);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride*metrics.widthPixels;

                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels+rowPadding/rowStride
                        , metrics.heightPixels, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);

                image.close();
                reader.close();


                String filename = "screenshot.png";
                FileOutputStream fos;
                try {
                    fos = FloatingViewService.this.openFileOutput(filename, MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent cropIntent = new Intent(FloatingViewService.this, ImageCropperActivity.class);
                cropIntent.putExtra("image", filename);
                cropIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(cropIntent);
            }
        }, null);
    }



    private boolean isViewCollapsed() {
        return FloatingView == null || FloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (FloatingView != null) wm.removeView(FloatingView);
    }
}
