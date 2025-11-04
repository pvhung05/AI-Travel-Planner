package com.example.ai_travel_planner.ai;

import android.content.Context;
import android.text.TextUtils;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.ai.GeminiGateway;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DescriptionService {
    private static final long STALE_AFTER_MS = 3L * 24 * 3600 * 1000; // 3 days
    private static final ExecutorService bg = Executors.newFixedThreadPool(2);

    public interface Callback {
        void onImmediate(String draftOrCached);
        void onShortReady(String shortText);
        void onLongReady(String longText);
    }

    public static String buildPlaceKey(String name, String vicinity) {
        String n = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        String v = vicinity == null ? "" : vicinity.trim().toLowerCase(Locale.ROOT);
        return n + "|" + v;
    }

    private static String instantDraft(String name, String vicinity, Double rating) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(name)) {
            sb.append(name);
        }
        if (!TextUtils.isEmpty(vicinity)) {
            if (sb.length() > 0) sb.append(" — ");
            sb.append(vicinity);
        }
        sb.append(". ");
        if (rating != null && rating > 0) {
            sb.append("Được đánh giá khoảng ").append(String.format(Locale.US, "%.1f/5", rating)).append(". ");
        }
        sb.append("Không khí thân thiện, phù hợp dạo chơi và chụp ảnh. ");
        sb.append("Gợi ý: ghé vào khung giờ ít đông để trải nghiệm tốt hơn.");
        return sb.toString().trim();
    }

    public static void getDescriptionProgressive(Context ctx,
                                                 String name,
                                                 String vicinity,
                                                 Double rating,
                                                 Callback cb) {
        final String placeKey = buildPlaceKey(name, vicinity);
        DescriptionCache.Entry cached = DescriptionCache.get(ctx, placeKey);
        long now = System.currentTimeMillis();

        if (cached != null && !TextUtils.isEmpty(cached.longText)) {
            cb.onImmediate(cached.longText);
            if (now - cached.updatedAt > STALE_AFTER_MS) {
                regenerateLongAsync(ctx, name, vicinity, rating, placeKey, null);
            }
            return;
        }

        if (cached != null && !TextUtils.isEmpty(cached.shortText)) {
            cb.onImmediate(cached.shortText);
            regenerateLongAsync(ctx, name, vicinity, rating, placeKey, cb);
            return;
        }

        String draft = instantDraft(name, vicinity, rating);
        cb.onImmediate(draft);
        DescriptionCache.saveShort(ctx, placeKey, draft);

        generateShortAsync(ctx, name, vicinity, rating, placeKey, cb);
        regenerateLongAsync(ctx, name, vicinity, rating, placeKey, cb);
    }

    public static void prefetch(Context ctx, String name, String vicinity, Double rating) {
        final String placeKey = buildPlaceKey(name, vicinity);
        DescriptionCache.Entry cached = DescriptionCache.get(ctx, placeKey);
        if (cached != null && !TextUtils.isEmpty(cached.longText)) return;
        generateShortAsync(ctx, name, vicinity, rating, placeKey, null);
        regenerateLongAsync(ctx, name, vicinity, rating, placeKey, null);
    }

    private static void generateShortAsync(Context ctx,
                                           String name,
                                           String vicinity,
                                           Double rating,
                                           String placeKey,
                                           Callback cb) {
        bg.submit(() -> {
            try {
                String apiKey = ctx.getString(R.string.gemini_api_key);
                GeminiGateway gw = new GeminiGateway(apiKey, "gemini-2.0-flash");

                String system = "Bạn là biên tập viên du lịch. Viết khách quan, súc tích, thân thiện.";
                JSONObject schema = new JSONObject();
                schema.put("points", new JSONArray());
                schema.put("summary", "");

                JSONObject ctxJo = new JSONObject();
                ctxJo.put("name", name);
                ctxJo.put("vicinity", vicinity);
                if (rating != null) ctxJo.put("rating", rating);

                String user = "Tạo JSON gồm 4–6 bullet (≤24 từ mỗi bullet) và 1 summary 1–2 câu. " +
                        "Không bịa chi tiết. Ngôn ngữ: vi.\n\n" +
                        "Context: " + ctxJo.toString() + "\n" +
                        "Schema: {\"points\":[{\"heading\":\"...\",\"content\":\"...\"}], \"summary\":\"...\"}\n" +
                        "Chỉ trả JSON hợp lệ.";

                String raw = gw.callGemini(system, user).get();
                String shortText = composeShortFromJson(raw, name);
                if (!TextUtils.isEmpty(shortText)) {
                    DescriptionCache.saveShort(ctx, placeKey, shortText);
                    if (cb != null) cb.onShortReady(shortText);
                }
            } catch (Exception ignored) { }
        });
    }

    private static void regenerateLongAsync(Context ctx,
                                            String name,
                                            String vicinity,
                                            Double rating,
                                            String placeKey,
                                            Callback cb) {
        bg.submit(() -> {
            try {
                String apiKey = ctx.getString(R.string.gemini_api_key);
                GeminiGateway gw = new GeminiGateway(apiKey, "gemini-2.0-flash");

                String system = "Bạn là biên tập viên du lịch. Viết tự nhiên, giàu thông tin, không bịa đặt.";
                JSONObject ctxJo = new JSONObject();
                ctxJo.put("name", name);
                ctxJo.put("vicinity", vicinity);
                if (rating != null) ctxJo.put("rating", rating);

                String user = "Viết giới thiệu 120–180 từ, mượt mà, dễ đọc, gồm: \n" +
                        "- Mở đầu gói gọn vibe và loại địa điểm.\n" +
                        "- 2–3 ý nổi bật.\n" +
                        "- Gợi ý thời điểm/tip nhỏ.\n" +
                        "Tránh số liệu cụ thể nếu không chắc chắn. Ngôn ngữ: vi.\n\n" +
                        "Context: " + ctxJo.toString();

                String longText = gw.callGemini(system, user).get();
                if (!TextUtils.isEmpty(longText)) {
                    String clean = cleanMarkdown(longText);
                    DescriptionCache.saveLong(ctx, placeKey, clean);
                    if (cb != null) cb.onLongReady(clean);
                }
            } catch (Exception ignored) { }
        });
    }

    private static String composeShortFromJson(String raw, String name) {
        try {
            JSONObject jo = new JSONObject(extractJson(raw));
            JSONArray points = jo.optJSONArray("points");
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(name)) {
                sb.append(name).append(". ");
            }
            if (points != null) {
                for (int i = 0; i < points.length(); i++) {
                    JSONObject p = points.optJSONObject(i);
                    if (p == null) continue;
                    String content = p.optString("content", null);
                    if (!TextUtils.isEmpty(content)) sb.append(content).append(" ");
                }
            }
            String summary = jo.optString("summary", null);
            if (!TextUtils.isEmpty(summary)) sb.append(summary);
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        int i = s.indexOf('{');
        int j = s.lastIndexOf('}');
        if (i >= 0 && j > i) return s.substring(i, j + 1);
        return "{}";
    }

    private static String cleanMarkdown(String s) {
        if (s == null) return "";
        return s.replace("**", "")
                .replace("*", "")
                .replace("__", "")
                .replace("_", "")
                .replace("###", "")
                .replace("##", "")
                .replace("#", "")
                .trim();
    }
}


