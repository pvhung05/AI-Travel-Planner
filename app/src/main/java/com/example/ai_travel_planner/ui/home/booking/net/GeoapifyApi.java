package com.example.ai_travel_planner.ui.home.booking.net;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeoapifyApi {
    // https://api.geoapify.com/v1/geocode/autocomplete?text=...&limit=10&apiKey=KEY
    @GET("v1/geocode/autocomplete")
    Call<GeoapifyResponse> autocomplete(
            @Query("text") String text,
            @Query("limit") int limit,
            @Query("apiKey") String apiKey,
            @Query("filter") String filter,   // ví dụ: "countrycode:vn" (có thể null)
            @Query("lang") String lang        // "en" hoặc "vi"
    );
}