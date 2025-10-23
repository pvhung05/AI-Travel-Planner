// app/src/main/java/com/example/ai_travel_planner/network/ai/GeminiClient.java
package com.example.ai_travel_planner.network.ai;

import android.content.Context;
import com.example.ai_travel_planner.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiClient {

    private static final String MODEL = "gemini-2.5-flash";
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1/models/" + MODEL + ":generateContent";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(80, TimeUnit.SECONDS)
            .writeTimeout(80, TimeUnit.SECONDS)
            .build();

    public static String callGemini(Context ctx, String promptText) throws IOException {
        String apiKey = ctx.getString(R.string.gemini_api_key);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("API key rỗng. Kiểm tra strings.xml (gemini_api_key).");
        }

        HttpUrl url = HttpUrl.parse(BASE_URL)
                .newBuilder()
                .addQueryParameter("key", apiKey)
                .build();

        // ---- BODY TỐI THIỂU: CHỈ contents ----
        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        content.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", promptText);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        root.add("contents", contents);

        RequestBody body = RequestBody.create(
                root.toString().getBytes(StandardCharsets.UTF_8), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response resp = client.newCall(request).execute()) {
            String responseBody = resp.body() != null ? resp.body().string() : "";
            if (!resp.isSuccessful()) {
                // In nguyên body lỗi để biết chính xác 400 vì lý do gì
                throw new IOException("HTTP " + resp.code() + " - " + responseBody);
            }
            return responseBody;
        }
    }
}