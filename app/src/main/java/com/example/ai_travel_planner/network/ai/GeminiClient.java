// app/src/main/java/com/example/ai_travel_planner/network/ai/GeminiClient.java
package com.example.ai_travel_planner.network.ai;

import android.content.Context;
import com.example.ai_travel_planner.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.ai_travel_planner.BuildConfig;
import com.example.ai_travel_planner.network.models.TripPlan;

public class GeminiClient {

    private static final String MODEL = "gemini-2.5-flash";
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1/models/" + MODEL + ":generateContent";
    private static final MediaType JSON = MediaType.parse("application/json;" +
            " charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
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

    public java.util.concurrent.CompletableFuture<TripPlan> generatePlanJson(com.example.ai_travel_planner.network.models.AiPlanRequest req) {

        String systemPrompt =
                "You are a travel planner. Return ONLY valid JSON (no markdown, no prose).\n" +
                        "Schema:\n" +
                        "{\n" +
                        "  \"city\": \"string\",\n" +
                        "  \"currency\": \"string\",\n" +
                        "  \"days\": [\n" +
                        "    {\n" +
                        "      \"label\": \"Day 1\",\n" +
                        "      \"date\": \"YYYY-MM-DD\",\n" +
                        "      \"activities\": [\n" +
                        "        {\"time\":\"HH:mm\",\"title\":\"string\",\"category\":\"food|nature|culture|shopping|adventure|other\",\"location\":\"string|null\",\"costVND\":0,\"notes\":\"string|null\",\"emoji\":\"string|null\"}\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

        String userPrompt =
                "Destination: " + req.getDestination() + "\n" +
                        "Budget: " + req.getBudget() + "\n" +
                        "Days: " + req.getNumDays() + "\n" +
                        "Style: " + req.getStyle() + "\n" +
                        "Return JSON only. Times must be HH:mm. Include a date per day.";

        // Giả sử bạn đã có apiService.callGemini(system, user) trả về raw text
        GeminiGateway gw = new GeminiGateway("AIzaSyBNxoQ74kz-_cm9qhXE6wWRzR4-gS9QL-4", "gemini-1.5-flash");

        return gw.callGemini(systemPrompt, userPrompt)
                .thenApply(json -> new Gson().fromJson(json, TripPlan.class));
    }

    // === THÊM helper ===
    private String extractPureJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("{") || s.startsWith("[")) return s;
        int i = s.indexOf('{');
        int j = s.lastIndexOf('}');
        return (i >= 0 && j > i) ? s.substring(i, j + 1) : "{}";
    }
}