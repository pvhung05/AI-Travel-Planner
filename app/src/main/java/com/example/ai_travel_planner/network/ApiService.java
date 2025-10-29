package com.example.ai_travel_planner.network;

import com.example.ai_travel_planner.network.models.LoginRequest;
import com.example.ai_travel_planner.network.models.LoginResponse;
import com.example.ai_travel_planner.network.models.RegisterRequest;
import com.example.ai_travel_planner.network.models.RegisterResponse;
import com.example.ai_travel_planner.network.models.UserMe;
import com.example.ai_travel_planner.network.models.WeatherResponse;
import com.example.ai_travel_planner.ui.home.plan.PlaceItem;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("/api/user/me")
    Call<UserMe> getMe(@Header("Authorization") String bearerToken);

    @Multipart
    @PUT("/api/user/profile")
    Call<UserMe> updateProfile(@Header("Authorization") String authToken,
                               @Part("displayName") RequestBody displayName,
                               @Part("email") RequestBody email,
                               @Part("password") RequestBody password,
                               @Part MultipartBody.Part avatarUrl
    );

    @GET("/api/places/most-viewed")
    Call<List<PlaceItem>> getMostViewedPlaces();

    @GET("/api/places/latest")
    Call<List<PlaceItem>> getLatestPlaces();
}
