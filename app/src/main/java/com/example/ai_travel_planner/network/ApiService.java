package com.example.ai_travel_planner.network;

import com.example.ai_travel_planner.network.models.LoginRequest;
import com.example.ai_travel_planner.network.models.LoginResponse;
import com.example.ai_travel_planner.network.models.RegisterRequest;
import com.example.ai_travel_planner.network.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
}
