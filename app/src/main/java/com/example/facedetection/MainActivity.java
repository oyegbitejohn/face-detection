package com.example.facedetection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static final int READ_CODE_REQUEST = 42;
    private Button loadImageBtn;
    private ImageView showImageView;
    private Button detectFaceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadImageBtn = findViewById(R.id.loadImage);
        showImageView = findViewById(R.id.image);
        detectFaceBtn = findViewById(R.id.detectFace);

        loadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // You can load from device or from the drawable folder.
//                loadImageFromDrawable();
                loadImageFromDevice();
            }
        });
    }

    public void loadImageFromDrawable() {
        Bitmap myBitmap = getBitmapFromImageSource(R.drawable.bust_size_photo_small);
        showImageView.setImageBitmap(myBitmap);

        Paint myRectPaint = getPaint(50F, Color.RED);
        // Create canvas to draw rectangle using the dimensions from the image's bitmap.
        Bitmap tempBitmap = Bitmap.createBitmap(
                myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565
        );
        // Create canvas to draw rectangle using the dimensions from the image's bitmap.
        Canvas tempCanvas = getCanvas(tempBitmap, myBitmap);


        activateDetectFaceBtn();
        detectFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectFace(myBitmap, tempBitmap, tempCanvas, myRectPaint);
            }
        });
    }

    public void activateDetectFaceBtn () {
        detectFaceBtn.setEnabled(true);
        detectFaceBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.success_green));
        detectFaceBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
    }

    public void loadImageFromDevice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(intent, READ_CODE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_CODE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    FileDescriptor fileDescriptor = getFileDescriptorFromUri(uri);
                    showImage(fileDescriptor);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showImage(FileDescriptor fileDescriptor) {
//        Bitmap myBitmap = getBitmapFromImageSource(fileDescriptor);
        Bitmap myBitmap = getBitmapFromImageSource(R.drawable.bust_size_photo_small);
        showImageView.setImageBitmap(myBitmap);

        Paint myRectPaint = getPaint(50F, Color.RED);

        Bitmap tempBitmap = Bitmap.createBitmap(
                myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565
        );
        // Create canvas to draw rectangle using the dimensions from the image's bitmap.
        Canvas tempCanvas = getCanvas(tempBitmap, myBitmap);

//        activateDetectFaceBtn();
//        detectFaceBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                detectFace(myBitmap, tempBitmap, tempCanvas, myRectPaint);
//            }
//        });
//        detectFace(myBitmap, tempBitmap, tempCanvas, myRectPaint);
    }

    public void detectFace(Bitmap myBitmap, Bitmap tempBitmap, Canvas tempCanvas, Paint myRectPaint) {
        // Create face detection.
        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        if (!faceDetector.isOperational()) {
            String message = "Face Detector is not functional";
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Detect faces.
        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        displayProgress(faces.size() + " faces detected! (from Device)");

        //Mark out the identified face.
        for (int i = 0; i < faces.size(); i++) {
            Face currFace = faces.valueAt(i);
            float left = currFace.getPosition().x;
            float top = currFace.getPosition().y;
            float right = left + currFace.getWidth();
            float bottom = right + currFace.getHeight();
            RectF rectF = new RectF(left, top, right, bottom);
            tempCanvas.drawRoundRect(rectF, 10F, 10F, myRectPaint);
        }

        showImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        // Release the FaceDetector.
        faceDetector.release();
    }

    public Bitmap getBitmapFromImageSource(int source) {
        // Load Image.
        // load your image from the file descriptor into memory and create a bitmap image from it.
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        // Since you will be updating this bitmap to paint over it when the face is detected,
        // you need to make it mutable.
        bitmapOptions.inMutable = true;
        Bitmap myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                source, // image from drawable. i.e, R.id.file_name.
                bitmapOptions
        );
        return myBitmap;
    }

    public Bitmap getBitmapFromImageSource(FileDescriptor source) {
        // Load Image.
        // load your image from the file descriptor into memory and create a bitmap image from it.
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        // Since you will be updating this bitmap to paint over it when the face is detected,
        // you need to make it mutable.
        bitmapOptions.inMutable = true;
        Bitmap myBitmap = BitmapFactory.decodeFileDescriptor(source);
        return myBitmap;
    }

    public Canvas getCanvas(Bitmap tempBitmap, Bitmap myBitmap) {
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(myBitmap, 0F, 0F, null);
        return tempCanvas;
    }

    public Paint getPaint(float strokeWidth, int color) {
        // Create Paint object to draw square.
        // You will only draw around the face, and not paint the whole face.
        // To do this, set a thin stroke, give it a color, which in our case is red,
        // and set the style of paint to STROKE using Paint.Style.STROKE.
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(strokeWidth);
        myRectPaint.setColor(color);
        myRectPaint.setStyle(Paint.Style.STROKE);
        return myRectPaint;
    }

    public FileDescriptor getFileDescriptorFromUri(Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        return fileDescriptor;
    }

    public void displayProgress(String text) {
        TextView textView = findViewById(R.id.error);
        textView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.success_green));
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        textView.setText(text);
    }
}