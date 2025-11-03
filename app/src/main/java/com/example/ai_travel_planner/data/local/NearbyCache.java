package com.example.ai_travel_planner.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ai_travel_planner.ui.home.plan.PlaceItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NearbyCache {
    private static final String PREFS = "nearby_cache_v1";
    private static final long TTL_MS = 5 * 60 * 1000; // 5 minutes

    private static String key(double lat, double lon) {
        // bucket 0.01 degrees to avoid over-fragmentation
        double latB = Math.round(lat * 100.0) / 100.0;
        double lonB = Math.round(lon * 100.0) / 100.0;
        return "nearby:" + latB + ":" + lonB;
    }

    public static List<PlaceItem> get(Context ctx, double lat, double lon) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(key(lat, lon), null);
        if (raw == null) return null;
        try {
            JSONObject jo = new JSONObject(raw);
            long ts = jo.optLong("ts", 0);
            if (System.currentTimeMillis() - ts > TTL_MS) return null;
            JSONArray arr = jo.optJSONArray("items");
            if (arr == null) return null;
            List<PlaceItem> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                PlaceItem it = new PlaceItem();
                it.setName(o.optString("name", null));
                it.setVicinity(o.optString("vicinity", null));
                if (o.has("rating")) it.setRating(o.optDouble("rating"));
                if (o.has("lat")) it.setLatitude(o.optDouble("lat"));
                if (o.has("lon")) it.setLongitude(o.optDouble("lon"));
                it.setImageUrl(o.optString("img", null));
                list.add(it);
            }
            return list;
        } catch (JSONException e) {
            return null;
        }
    }

    public static void save(Context ctx, double lat, double lon, List<PlaceItem> items) {
        try {
            JSONArray arr = new JSONArray();
            if (items != null) {
                for (PlaceItem it : items) {
                    JSONObject o = new JSONObject();
                    o.put("name", it.getName());
                    o.put("vicinity", it.getVicinity());
                    if (it.getRating() != null) o.put("rating", it.getRating());
                    o.put("lat", it.getLatitude());
                    o.put("lon", it.getLongitude());
                    o.put("img", it.getImageUrl());
                    arr.put(o);
                }
            }
            JSONObject jo = new JSONObject();
            jo.put("ts", System.currentTimeMillis());
            jo.put("items", arr);
            SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            sp.edit().putString(key(lat, lon), jo.toString()).apply();
        } catch (JSONException ignored) { }
    }
}


