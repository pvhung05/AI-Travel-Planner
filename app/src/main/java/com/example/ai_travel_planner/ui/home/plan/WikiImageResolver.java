package com.example.ai_travel_planner.ui.home.plan;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WikiImageResolver {
    public static String resolve(String location) {
        try {
            String encoded = URLEncoder.encode(location, "UTF-8");
            String api = "https://en.wikipedia.org/w/api.php?action=query&titles=" + encoded +
                    "&prop=pageimages&format=json&pithumbsize=600";
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder().url(api).build();
            Response res = client.newCall(req).execute();
            String json = res.body().string();

            JSONObject obj = new JSONObject(json)
                    .getJSONObject("query")
                    .getJSONObject("pages");

            Iterator<String> keys = obj.keys();
            if (keys.hasNext()) {
                JSONObject page = obj.getJSONObject(keys.next());
                if (page.has("thumbnail")) {
                    return page.getJSONObject("thumbnail").getString("source");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // fallback
        return "https://source.unsplash.com/random/800x600/?" + location;
    }
}