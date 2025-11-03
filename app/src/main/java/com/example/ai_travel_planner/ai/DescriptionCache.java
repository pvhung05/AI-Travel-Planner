package com.example.ai_travel_planner.ai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class DescriptionCache {
    private static final String PREFS = "place_description_cache_v2";

    public static class Entry {
        public final String shortText;
        public final String longText;
        public final long updatedAt;

        public Entry(String shortText, String longText, long updatedAt) {
            this.shortText = shortText;
            this.longText = longText;
            this.updatedAt = updatedAt;
        }
    }

    private static String keyFor(String placeKey) {
        return "desc:" + placeKey;
    }

    public static Entry get(Context ctx, String placeKey) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString(keyFor(placeKey), null);
        if (raw == null) return null;
        try {
            JSONObject jo = new JSONObject(raw);
            String shortText = jo.optString("short", null);
            String longText = jo.optString("long", null);
            long updatedAt = jo.optLong("updatedAt", 0);
            return new Entry(shortText, longText, updatedAt);
        } catch (JSONException e) {
            return null;
        }
    }

    public static void saveShort(Context ctx, String placeKey, String shortText) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String k = keyFor(placeKey);
            JSONObject jo = new JSONObject(sp.getString(k, "{}"));
            jo.put("short", shortText);
            jo.put("updatedAt", System.currentTimeMillis());
            sp.edit().putString(k, jo.toString()).apply();
        } catch (JSONException ignored) { }
    }

    public static void saveLong(Context ctx, String placeKey, String longText) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String k = keyFor(placeKey);
            JSONObject jo = new JSONObject(sp.getString(k, "{}"));
            jo.put("long", longText);
            jo.put("updatedAt", System.currentTimeMillis());
            sp.edit().putString(k, jo.toString()).apply();
        } catch (JSONException ignored) { }
    }
}


