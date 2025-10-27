package com.example.ai_travel_planner.ui.home.booking.net;

import com.example.ai_travel_planner.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeoService {

    private static GeoapifyApi api;

    public static GeoapifyApi api() {
        if (api == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(log)
                    .build();

            Retrofit r = new Retrofit.Builder()
                    .baseUrl("https://api.geoapify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            api = r.create(GeoapifyApi.class);
        }
        return api;
    }

    public static String geoKey() {
        return BuildConfig.GEOAPIFY_KEY == null ? "" : BuildConfig.GEOAPIFY_KEY;
    }
}