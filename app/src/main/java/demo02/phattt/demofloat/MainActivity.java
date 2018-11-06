package demo02.phattt.demofloat;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 123;
    private MediaProjectionManager mediaProjectionManager;
    private Intent floatViewService;
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startService(new Intent(MainActivity.this, FloatingViewService.class));
                floatViewService = new Intent(MainActivity.this, FloatingViewService.class);
                mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Access denied!", Toast.LENGTH_SHORT).show();
                return;
            }
//            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            floatViewService.putExtra("resultCode", resultCode);
            floatViewService.putExtra("data", data);
            floatViewService.putExtra("code", 1);
            startService(floatViewService);
            MainActivity.this.finish();
        }
    }
}
