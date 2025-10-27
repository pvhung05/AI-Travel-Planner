package com.example.ai_travel_planner.ui.home.booking.net;

import android.content.Context;

import com.example.ai_travel_planner.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookingService {
    private static Retrofit retrofit;

    public static BookingApi api(Context ctx) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request req = original.newBuilder()
                                .addHeader("x-rapidapi-key", ctx.getString(R.string.rapidapi_key))
                                .addHeader("x-rapidapi-host", "booking-com15.p.rapidapi.com")
                                .build();
                        return chain.proceed(req);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://booking-com15.p.rapidapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(BookingApi.class);
    }
}