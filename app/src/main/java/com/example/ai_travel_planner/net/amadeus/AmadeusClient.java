package com.example.ai_travel_planner.net.amadeus;

import android.util.Log;

import com.example.ai_travel_planner.BuildConfig;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AmadeusClient {

    private static Retrofit retrofit;
    private static volatile String bearer = null; // cache token (simple in-memory)
    private static long expireAtMs = 0L;

    public static Retrofit authed() {
        if (retrofit == null) {
            synchronized (AmadeusClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient http = new OkHttpClient.Builder()
                            .addInterceptor(new OAuthInterceptor())
                            .addInterceptor(log)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(90, TimeUnit.SECONDS)
                            .writeTimeout(90, TimeUnit.SECONDS)
                            .callTimeout(90, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.AMADEUS_BASE)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(http)
                            .build();
                }
            }
        }
        return retrofit;
    }

    /** Interceptor chèn Bearer token, tự làm mới khi hết hạn */
    static class OAuthInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            String token = token();
            Request req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            Response res = chain.proceed(req);
            if (res.code() == 401) {
                // refresh rồi thử lại 1 lần
                synchronized (AmadeusClient.class) { bearer = null; expireAtMs = 0; }
                String t2 = token();
                req = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + t2)
                        .build();
                res.close();
                return chain.proceed(req);
            }
            return res;
        }
    }

    private static String token() throws IOException {
        if (bearer != null && System.currentTimeMillis() < expireAtMs - 10_000) return bearer;

        OkHttpClient naked = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .callTimeout(90, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        FormBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", BuildConfig.AMADEUS_KEY)
                .add("client_secret", BuildConfig.AMADEUS_SECRET)
                .build();

        Request req = new Request.Builder()
                .url(BuildConfig.AMADEUS_OAUTH)
                .post(body)
                .build();

        try (Response r = naked.newCall(req).execute()) {
            if (!r.isSuccessful()) throw new IOException("OAuth fail " + r.code());
            String json = r.body().string();
            JsonObject o = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            bearer = o.get("access_token").getAsString();
            int expires = o.get("expires_in").getAsInt(); // seconds
            expireAtMs = System.currentTimeMillis() + expires * 1000L;
            Log.d("AMA", "new token ok, exp " + expires + "s");
            return bearer;
        }
    }
}