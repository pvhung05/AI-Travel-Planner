package com.example.ai_travel_planner.network.models;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeoRequest {

    @GET("places")
    Call<GeoResponse> getNearbyPlaces(
            @Query("categories") String categories,
            @Query("filter") String filter,     // ví dụ: circle:lng,lat,radius
            @Query("bias") String bias,         // ví dụ: proximity:lng,lat
            @Query("limit") int limit,
            @Query("apiKey") String apiKey
    );
}
