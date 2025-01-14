package com.facedetect.facepick;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facedetect.facepick.databinding.ActivityMainBinding;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String API_KEY = "lTFpZAadM01uP10IB6BdfTL2iBzkvUqFmA8SQa5L";
    private ApiService apiService;
    private RectangleDrawingImageView imageView;

    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        imageView = findViewById(R.id.imageIV);

        apiService = ApiClient.getClient().create(ApiService.class);

        activityMainBinding.clickBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityMainBinding.animLV.setVisibility(View.GONE);
                activityMainBinding.imageIV.setVisibility(View.VISIBLE);
                openGalleryOrCamera();
            }
        });



    }

    private void openGalleryOrCamera() {
        clearImageview();
        // Start gallery intent
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void clearImageview() {
        RectangleDrawingImageView imageView = findViewById(R.id.imageIV);
        imageView.clearRectangles();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            // Get the selected image
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());


                Glide.with(this)
                        .asBitmap()
                        .load(bitmap)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                int originalWidth = resource.getWidth();
                                int originalHeight = resource.getHeight();

                                // Pass original dimensions to RectangleDrawingImageView
                                imageView.setOriginalImageSize(originalWidth, originalHeight);

                                // After getting image dimensions, call your API to detect faces
                                // Example:
                                // callFaceDetectionAPI();
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Placeholder cleared
                            }
                        });


                activityMainBinding.imageIV.setImageBitmap(bitmap);

                // Convert bitmap to file
                File file = bitmapToFile(bitmap);

                // Call API
                if (file != null) {
                    activityMainBinding.progressBar.setVisibility(View.VISIBLE);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    Call<List<FaceDetectionResult>> call = apiService.detectFaces(requestFile, API_KEY);
                    call.enqueue(new Callback<List<FaceDetectionResult>>() {
                        @Override
                        public void onResponse(Call<List<FaceDetectionResult>> call, Response<List<FaceDetectionResult>> response) {
                            if (response.isSuccessful()) {
                                // Handle API response and draw rectangle
                                List<FaceDetectionResult> dataList = response.body();
                                if (dataList != null && dataList.size() > 0) {
                                    activityMainBinding.progressBar.setVisibility(View.GONE);
                                    drawRectangle(dataList);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to get response", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<FaceDetectionResult>> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File bitmapToFile(Bitmap bitmap) {
        try {
            // Create a file to write bitmap data
            File file = new File(getCacheDir(), "image.jpg");
            file.createNewFile();

            // Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();

            // Write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void drawRectangle(List<FaceDetectionResult> dataList ) {
        RectangleDrawingImageView imageView = findViewById(R.id.imageIV);
        imageView.addRectangles(dataList);
        Toast.makeText(MainActivity.this,String.valueOf(dataList.size())+" : Face Detected",Toast.LENGTH_SHORT).show();
    }
}