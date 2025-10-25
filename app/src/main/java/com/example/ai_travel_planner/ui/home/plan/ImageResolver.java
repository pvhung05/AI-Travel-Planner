// app/src/main/java/com/example/ai_travel_planner/ui/home/plan/ImageResolver.java
package com.example.ai_travel_planner.ui.home.plan;

import android.content.Context;
import android.text.TextUtils;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.images.UnsplashApi;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageResolver {

    private static UnsplashApi unsplash;

    private static UnsplashApi getUnsplash() {
        if (unsplash == null) {
            Retrofit r = new Retrofit.Builder()
                    .baseUrl("https://api.unsplash.com/")
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            unsplash = r.create(UnsplashApi.class);
        }
        return unsplash;
    }

    public static String normalize(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.US).trim();
    }

    /** URL fallback qua Unsplash Source – có sig để tránh cache trùng */
    public static String buildFallbackUrl(String destination) {
        try {
            String q = URLEncoder.encode(destination + " city skyline landmark travel", "UTF-8");
            String sig = String.valueOf(System.currentTimeMillis());
            return "https://source.unsplash.com/random/800x600/?" + q + "&sig=" + sig;
        } catch (Exception e) {
            return "https://source.unsplash.com/random/800x600/?city,landmark&sig=" + System.currentTimeMillis();
        }
    }

    /** Lấy ảnh Wikipedia theo page title (không cần API key) */
    private static String tryWikipedia(String destination) {
        try {
            String encoded = URLEncoder.encode(destination, "UTF-8");
            String api = "https://en.wikipedia.org/w/api.php?action=query&titles=" + encoded +
                    "&prop=pageimages&format=json&pithumbsize=800";
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder().url(api).build();
            Response res = client.newCall(req).execute();
            String json = res.body() != null ? res.body().string() : null;
            if (json == null) return null;

            JSONObject pages = new JSONObject(json)
                    .getJSONObject("query")
                    .getJSONObject("pages");
            Iterator<String> keys = pages.keys();
            if (keys.hasNext()) {
                JSONObject page = pages.getJSONObject(keys.next());
                if (page.has("thumbnail")) {
                    return page.getJSONObject("thumbnail").getString("source");
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    /** Lấy ảnh từ Unsplash API chính thức (cần access key) */
    private static String tryUnsplash(Context ctx, String destination) {
        try {
            String key = ctx.getString(R.string.unsplash_access_key);
            if (TextUtils.isEmpty(key)) return null; // không cấu hình thì bỏ qua

            // CHÚ Ý: query phải là câu chữ, KHÔNG phải URL
            String q = destination + " city skyline landmark";
            retrofit2.Response<UnsplashApi.SearchResponse> resp =
                    getUnsplash().searchPhotos(q, 1, "landscape", key).execute();
            if (resp.isSuccessful() && resp.body() != null &&
                    resp.body().results != null && !resp.body().results.isEmpty()) {
                UnsplashApi.Urls urls = resp.body().results.get(0).urls;
                if (urls != null && !TextUtils.isEmpty(urls.regular)) return urls.regular;
                if (urls != null) return urls.small;
            }
        } catch (Exception ignore) {}
        return null;
    }

    /** API tổng: cố Wikipedia → Unsplash API → fallback Unsplash Source */
    public static String resolve(Context ctx, String destination) {
        // Wikipedia – đúng địa danh nhất
        String wiki = tryWikipedia(destination);
        if (!TextUtils.isEmpty(wiki)) return wiki;

        // Unsplash chính thức
        String up = tryUnsplash(ctx, destination);
        if (!TextUtils.isEmpty(up)) return up;

        // Fallback tránh trùng
        return buildFallbackUrl(destination);
    }
}