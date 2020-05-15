package com.example.visual;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SearchEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    //buttons on the xml layout
    private ImageButton visual_search;
    private EditText keywords;

    //permission codes for access.
    public static final int ask_permission=1;
    public static final int ask_image=2;
    public static final int pic_crop = 1;

    private Uri PictureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("Main activity","Inside Oncreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check for the necessary permissions

        //camera permission
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, ask_permission);
        }
        //permission to write to storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ask_permission);
        }
        //permission to read from storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, ask_permission);
        }

        visual_search=(ImageButton)findViewById(R.id.camera);
        visual_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("Main activity","Inside onclick");
                ContentValues content = new ContentValues();
                content.put(MediaStore.Images.Media.TITLE, "New pic");
                content.put(MediaStore.Images.Media.DESCRIPTION, "From visual app");
                PictureUri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, PictureUri);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                startActivityForResult(cameraIntent,ask_image);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.v("Main activity","Inside on request permissions result");
        switch (requestCode) {
            case ask_permission: {
                Toast info = Toast.makeText(getApplicationContext(),"This feature needs permission to camera, read, write.Please allow and try again :)",Toast.LENGTH_LONG);
                info.show();
                System.exit(0);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("Main activity","Inside on activity result");
        if(requestCode == ask_image && resultCode == RESULT_OK) {
            Log.v("Mai n activity","Inside got image from camera");
            performCrop(PictureUri);
        }
        if (requestCode == pic_crop) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                Bitmap selectedBitmap = extras.getParcelable("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Intent search_intent = new Intent(MainActivity.this, Search.class);
                search_intent.putExtra("image",byteArray);
                startActivity(search_intent);
            }
        }
    }

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, pic_crop);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
