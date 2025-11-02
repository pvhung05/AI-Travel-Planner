package com.example.ai_travel_planner.network;

import com.example.ai_travel_planner.network.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat, // Vĩ độ
            @Query("lon") double lon, // Kinh độ
            @Query("appid") String apiKey, // API Key của bạn
            @Query("units") String units // Dùng "metric" để lấy độ C
    );
}