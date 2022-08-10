package com.flyai.StyleTransfer;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    // Elements for flip pages
    ViewFlipper viewFlipper;
    Button afterButton;
    Button beforeButton;
    // Elements for style
    Button styleGallery;
    Button styleCamera;
    ImageView styleImage;
    final int styleIndex = 0;
    Uri styleUri;
    // Elements for content
    Button contentGallery;
    Button contentCamera;
    ImageView contentImage;
    final int contentIndex = 1;
    Uri contentUri;
    // Elements for result
    TextView progressText;
    ImageView resultImage;
    Button saveResult;
    Uri resultUri;
    final int resultIndex = 2;
    // Constants
    final int PICK_STYLE_GALLERY = 3;
    final int PICK_STYLE_CAMERA = 4;
    final int PICK_CONTENT_GALLERY = 5;
    final int PICK_CONTENT_CAMERA = 6;
    String basePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        viewFlipper = (ViewFlipper)  findViewById(R.id.viewFlipper);
        afterButton = (Button) findViewById(R.id.afterButton);
        beforeButton = (Button) findViewById(R.id.beforeButton);
        styleGallery = (Button) findViewById(R.id.styleGallery);
        styleCamera = (Button) findViewById(R.id.styleCamera);
        styleImage = (ImageView) findViewById(R.id.styleImage);
        contentGallery = (Button) findViewById(R.id.contentGallery);
        contentCamera = (Button) findViewById(R.id.contentCamera);
        contentImage = (ImageView) findViewById(R.id.contentImage);
        progressText = (TextView) findViewById(R.id.progressText);
        resultImage = (ImageView) findViewById(R.id.resultImage);
        saveResult = (Button) findViewById(R.id.saveResult);
        basePath = getApplicationContext().getFilesDir().getAbsolutePath();

        // Page actions
        afterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (viewFlipper.getDisplayedChild()) {
                    case styleIndex: {
                        beforeButton.setEnabled(true);
                        if (contentImage.getVisibility() == View.GONE) { afterButton.setEnabled(false); }
                        viewFlipper.showNext();
                        break;
                    }
                    case contentIndex: {
                        afterButton.setText("처음부터");
                        if (resultImage.getVisibility() == View.GONE) { afterButton.setEnabled(false); }
                        viewFlipper.showNext();
                        break;
                    }
                    case resultIndex: { // reset
                        beforeButton.setEnabled(false);
                        afterButton.setEnabled(false);
                        afterButton.setText("다음");
                        saveResult.setText("모델 훈련 시작");
                        styleImage.setVisibility(View.GONE);
                        contentImage.setVisibility(View.GONE);
                        resultImage.setVisibility(View.GONE);
                        viewFlipper.showNext();
                        break;
                    }
                }
            }
        });
        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (viewFlipper.getDisplayedChild()) {
                    case styleIndex: {
                        break;
                    }
                    case contentIndex: {
                        beforeButton.setEnabled(false);
                        if (styleImage.getVisibility() == View.VISIBLE) { afterButton.setEnabled(true); }
                        viewFlipper.showPrevious();
                        break;
                    }
                    case resultIndex: {
                        afterButton.setText("다음");
                        if (contentImage.getVisibility() == View.VISIBLE) { afterButton.setEnabled(true); }
                        viewFlipper.showPrevious();
                        break;
                    }
                }
            }
        });

        // Pick Images
        styleGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_STYLE_GALLERY);
            }
        });
        styleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(basePath + "/style.jpg");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(
                                getApplicationContext(),
                                "com.flyai.StyleTransfer.fileprovider",
                                file
                ));
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PICK_STYLE_CAMERA);
                }
                else {
                    startActivityForResult(takePictureIntent, PICK_STYLE_CAMERA);
                }
            }
        });
        contentGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_CONTENT_GALLERY);
            }
        });
        contentCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(basePath +"/content.jpg");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(
                                getApplicationContext(),
                                "com.flyai.StyleTransfer.fileprovider",
                                file
                        ));
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PICK_CONTENT_CAMERA);
                }
                else {
                    startActivityForResult(takePictureIntent, PICK_CONTENT_CAMERA);
                }
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch (requestCode) {
            case PICK_STYLE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    File file = new File(basePath +"/style.jpg");
                    takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(
                                    getApplicationContext(),
                                    "com.flyai.StyleTransfer.fileprovider",
                                    file
                            ));
                    startActivityForResult(takePictureIntent, requestCode);
                } else {
                    Toast.makeText(MainActivity.this, "권한을 허용해야 합니다", Toast.LENGTH_LONG).show();
                }
                break;
            case PICK_CONTENT_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    File file = new File(basePath +"/content.jpg");
                    takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(
                                    getApplicationContext(),
                                    "com.flyai.StyleTransfer.fileprovider",
                                    file
                            ));
                    startActivityForResult(takePictureIntent, requestCode);
                } else {
                    Toast.makeText(MainActivity.this, "권한을 허용해야 합니다", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_STYLE_GALLERY: {
                try {
                    styleUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(styleUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    styleImage.setImageBitmap(selectedImage);
                    styleImage.setVisibility(View.VISIBLE);
                    afterButton.setEnabled(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "오류가 발생했습니다", Toast.LENGTH_LONG).show();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "사진을 선택해주세요", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PICK_STYLE_CAMERA: {
                try {
                    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                    bmpFactoryOptions.inSampleSize = 1;
                    Bitmap selectedImage = BitmapFactory.decodeFile(basePath + "/style.jpg", bmpFactoryOptions);
                    styleImage.setImageBitmap(selectedImage);
                    styleImage.setVisibility(View.VISIBLE);
                    afterButton.setEnabled(true);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "사진을 촬영해주세요", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PICK_CONTENT_GALLERY: {
                try {
                    contentUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(styleUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    contentImage.setImageBitmap(selectedImage);
                    contentImage.setVisibility(View.VISIBLE);
                    afterButton.setEnabled(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "오류가 발생했습니다", Toast.LENGTH_LONG).show();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "사진을 선택해주세요", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PICK_CONTENT_CAMERA: {
                try {
                    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                    bmpFactoryOptions.inSampleSize = 1;
                    Bitmap selectedImage = BitmapFactory.decodeFile(basePath + "/content.jpg", bmpFactoryOptions);
                    contentImage.setImageBitmap(selectedImage);
                    contentImage.setVisibility(View.VISIBLE);
                    afterButton.setEnabled(true);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "사진을 촬영해주세요", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}