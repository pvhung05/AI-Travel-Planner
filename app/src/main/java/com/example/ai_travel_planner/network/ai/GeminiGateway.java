package com.example.ai_travel_planner.network.ai;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiGateway {
    private static final String TAG = "GeminiGW";
    private static final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");

    // Thứ tự ưu tiên model (nếu có trong ListModels sẽ chọn theo thứ tự này)
    private static final List<String> DESIRED_MODELS = Arrays.asList(
            "gemini-2.5-flash",
            "gemini-2.5-flash-latest",
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
    );

    // Thử cả v1beta rồi v1 (vì một số model chỉ có ở v1beta)
    private static final List<String> API_VERSIONS = Arrays.asList("v1beta", "v1");

    private final String apiKey;
    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(80, TimeUnit.SECONDS)
            .writeTimeout(80, TimeUnit.SECONDS)
            .build();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public GeminiGateway(String apiKey, String ignoredModel) {
        this.apiKey = apiKey;
    }

    public CompletableFuture<String> callGemini(String systemPrompt, String userPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generate(systemPrompt, userPrompt);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, io);
    }

    public String generate(String systemPrompt, String userPrompt) throws IOException {
        // 1) Hỏi danh sách model khả dụng (ListModels)
        List<String> available = discoverModels();

        // 2) Lập danh sách model sẽ thử theo ưu tiên
        List<String> toTry = new ArrayList<>();
        for (String want : DESIRED_MODELS) {
            if (available.contains(want)) toTry.add(want);
        }
        // Nếu list trống (API trả về rỗng / lỗi), vẫn thử “chuẩn” để có cơ hội chạy
        if (toTry.isEmpty()) {
            toTry.addAll(DESIRED_MODELS);
        }

        // 3) Gọi generateContent theo thứ tự: v1beta rồi v1, với mỗi model trong toTry
        IOException last = null;
        for (String ver : API_VERSIONS) {
            for (String model : toTry) {
                try {
                    String out = callOnce(ver, model, systemPrompt, userPrompt);
                    Log.d(TAG, "OK with " + ver + " / " + model);
                    return out;
                } catch (IOException e) {
                    last = e;
                    Log.w(TAG, "Try fail " + ver + " / " + model + " : " + e.getMessage());
                }
            }
        }
        throw last != null ? last : new IOException("All endpoints failed");
    }

    /** Gọi GET /{ver}/models?key=... để lấy danh sách model và lọc những model hỗ trợ generateContent */
    private List<String> discoverModels() {
        List<String> result = new ArrayList<>();
        for (String ver : API_VERSIONS) {
            try {
                HttpUrl url = new HttpUrl.Builder()
                        .scheme("https")
                        .host("generativelanguage.googleapis.com")
                        .addPathSegment(ver)
                        .addPathSegment("models")
                        .addQueryParameter("key", apiKey)
                        .build();

                Request req = new Request.Builder().url(url).get().build();
                try (Response r = http.newCall(req).execute()) {
                    String t = r.body() != null ? r.body().string() : "";
                    Log.d(TAG, "ListModels " + ver + " (" + r.code() + "): " + trimForLog(t));
                    if (!r.isSuccessful()) continue;

                    JSONObject jo = new JSONObject(t);
                    JSONArray models = jo.optJSONArray("models");
                    if (models == null) continue;

                    for (int i = 0; i < models.length(); i++) {
                        JSONObject m = models.optJSONObject(i);
                        if (m == null) continue;
                        String fullName = m.optString("name", ""); // ví dụ "models/gemini-1.5-flash"
                        if (fullName.startsWith("models/")) fullName = fullName.substring("models/".length());

                        // Chỉ lấy model có hỗ trợ generateContent
                        JSONArray methods = m.optJSONArray("supportedGenerationMethods");
                        boolean ok = false;
                        if (methods != null) {
                            for (int j = 0; j < methods.length(); j++) {
                                if ("generateContent".equals(methods.optString(j))) {
                                    ok = true; break;
                                }
                            }
                        }
                        if (ok && !fullName.isEmpty() && !result.contains(fullName)) {
                            result.add(fullName);
                        }
                    }
                }
                if (!result.isEmpty()) break; // đã thu thập được → dừng
            } catch (Exception e) {
                Log.w(TAG, "ListModels " + ver + " failed: " + e.getMessage());
            }
        }
        return result;
    }

    private String callOnce(String version, String model, String systemPrompt, String userPrompt) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("generativelanguage.googleapis.com")
                .addPathSegment(version)
                .addPathSegment("models")
                .addPathSegment(model + ":generateContent")
                .addQueryParameter("key", apiKey)
                .build();

        // Body tương thích v1/v1beta: gộp system + user vào 1 message role=user
        JSONObject body = new JSONObject();
        try {
            String sys = (systemPrompt == null || systemPrompt.isEmpty())
                    ? ""
                    : "SYSTEM INSTRUCTION:\n" + systemPrompt + "\n\n";
            String u = (userPrompt == null) ? "" : userPrompt;

            JSONObject userObj = new JSONObject();
            userObj.put("role", "user");

            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", sys + u));
            userObj.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(userObj);
            body.put("contents", contents);
        } catch (JSONException e) {
            throw new IOException("Build JSON request failed", e);
        }

        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), MEDIA_JSON))
                .build();

        Log.d(TAG, "URL=" + url);
        try (Response resp = http.newCall(req).execute()) {
            String respText = (resp.body() != null) ? resp.body().string() : "";
            if (!resp.isSuccessful()) {
                Log.w(TAG, "HTTP " + resp.code() + " " + resp.message() + " " + respText);
                throw new IOException("HTTP " + resp.code() + " " + resp.message() + " " + respText);
            }
            return extractText(respText);
        }
    }

    private String extractText(String respText) {
        try {
            JSONObject jo = new JSONObject(respText);
            JSONArray candidates = jo.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                if (content != null) {
                    JSONArray parts = content.optJSONArray("parts");
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", respText);
                    }
                }
            }
        } catch (JSONException ignore) { }
        return respText;
    }

    private static String trimForLog(String s) {
        if (s == null) return "";
        s = s.replace("\n", "");
        return s.length() > 600 ? s.substring(0, 600) + "...(truncated)" : s;
    }
}
