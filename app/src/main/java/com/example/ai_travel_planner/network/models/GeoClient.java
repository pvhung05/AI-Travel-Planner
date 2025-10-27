package com.example.ai_travel_planner.network.models;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeoClient {
    private static final String BASE_URL = "https://api.geoapify.com/v2/";
    private static Retrofit retrofit;

    public static GeoRequest getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GeoRequest.class);
    }
}
