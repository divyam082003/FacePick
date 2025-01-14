package com.facedetect.facepick;

import com.google.gson.JsonObject;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import okhttp3.RequestBody;

public interface ApiService {
    @Multipart
    @POST("v1/facedetect")
    Call<List<FaceDetectionResult>> detectFaces(
            @Part("image\"; filename=\"image.jpg\" ") RequestBody file,
            @Header("X-Api-Key") String apiKey
    );
}
