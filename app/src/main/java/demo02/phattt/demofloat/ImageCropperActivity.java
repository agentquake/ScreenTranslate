package demo02.phattt.demofloat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageCropperActivity extends AppCompatActivity {
    private ImageView imageCrop;
    private Uri uri;
    private Bitmap image;
    private Intent floatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropper);
        imageCrop = findViewById(R.id.imgCrop);
        Intent intent = this.getIntent();
        String filename = intent.getStringExtra("image");
        File imgFile = new File(getFilesDir(), filename);
        uri = Uri.fromFile(imgFile);
        startCrop(uri);
//        imageCrop = findViewById(R.id.imgCrop);
//        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
    }

    private void startCrop(Uri uri){
        CropImage.activity(uri).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK){
                    Uri resUri = result.getUri();
                    image = BitmapFactory.decodeFile(resUri.getPath());
                    List<String> detectedTexts = getTextFromImage(image);

                    /////////////////////////Translate

                    List<String> translatedText;






                    /////////////////////////// Add history



                    floatService = new Intent(this, FloatingViewService.class); //create intent
                    floatService.putExtra("code", 2); //code request


                    floatService.putStringArrayListExtra("Texts", (ArrayList<String>) detectedTexts); //Raw text
//                    floatService.putStringArrayListExtra("TranslatedTexts", (ArrayList<String>)translatedText); //Translated text

                    startService(floatService);
                    finish();
                }
        }
    }

    private List<String> getTextFromImage(Bitmap image){
        TextRecognizer textRecognizer =  new TextRecognizer.Builder(this).build();
        Frame frame = new Frame.Builder().setBitmap(image).build();
        SparseArray<TextBlock> item = textRecognizer.detect(frame);

        List<String> texts = null;
        if (item.size() > 0) {
            texts = new ArrayList<>();
            for (int i  = 0; i < item.size(); i++){
                texts.add(item.valueAt(i).getValue());
            }
        }
        return texts;
    }
}
