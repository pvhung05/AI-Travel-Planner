package com.example.ai_travel_planner.ui.home.booking.net;

import com.example.ai_travel_planner.BuildConfig;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Net {

    private static BookingApi booking;

    public static BookingApi booking() {
        if (booking == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .addHeader("X-RapidAPI-Key", BuildConfig.RAPIDAPI_KEY)
                                    .addHeader("X-RapidAPI-Host", "booking-com.p.rapidapi.com")
                                    .build()
                    ))
                    .addInterceptor(log)
                    .build();

            booking = new Retrofit.Builder()
                    .baseUrl("https://booking-com.p.rapidapi.com/v1/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(BookingApi.class);
        }
        return booking;
    }
}