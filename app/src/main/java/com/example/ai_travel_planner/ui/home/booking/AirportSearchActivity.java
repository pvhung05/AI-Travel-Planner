package com.example.ai_travel_planner.ui.home.booking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.BuildConfig;
import com.example.ai_travel_planner.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AirportSearchActivity extends AppCompatActivity {

    public static final String EXTRA_IATA  = "extra_iata";
    public static final String EXTRA_LABEL = "extra_label";

    private EditText txtSearch;
    private ListView lvResults;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> display = new ArrayList<>();
    private final ArrayList<String> iatas   = new ArrayList<>();

    private final OkHttpClient http = new OkHttpClient();

    private static final String FALLBACK_GEO_KEY = "PUT_YOUR_GEOAPIFY_KEY";

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport_search);

        ImageButton btnClose = findViewById(R.id.btnClose);
        txtSearch  = findViewById(R.id.txtSearch);
        lvResults  = findViewById(R.id.lvResults);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, display);
        lvResults.setAdapter(adapter);

        btnClose.setOnClickListener(v -> finish());

        txtSearch.requestFocus();
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                if (q.length() >= 2) fetch(q);
                else {
                    display.clear(); iatas.clear(); adapter.notifyDataSetChanged();
                }
            }
        });

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent();
                it.putExtra(EXTRA_IATA,  iatas.get(position));
                it.putExtra(EXTRA_LABEL, display.get(position));
                setResult(Activity.RESULT_OK, it);
                finish();
            }
        });
    }

    private void fetch(String q) {
        new Thread(() -> {
            try {
                String key = BuildConfig.GEOAPIFY_KEY != null ? BuildConfig.GEOAPIFY_KEY : FALLBACK_GEO_KEY;
                String url = "https://api.geoapify.com/v1/geocode/autocomplete?limit=10&text="
                        + URLEncoder.encode(q, "UTF-8") + "&apiKey=" + key;

                Request req = new Request.Builder().url(url).get().build();
                try (Response resp = http.newCall(req).execute()) {
                    if (!resp.isSuccessful() || resp.body() == null) throw new IOException("HTTP " + resp.code());
                    String body = resp.body().string();

                    JSONObject root = new JSONObject(body);
                    JSONArray feats = root.optJSONArray("features");
                    ArrayList<String> d = new ArrayList<>();
                    ArrayList<String> ia = new ArrayList<>();

                    if (feats != null) {
                        for (int i = 0; i < feats.length(); i++) {
                            JSONObject f = feats.getJSONObject(i);
                            JSONObject p = f.optJSONObject("properties");
                            if (p == null) continue;

                            // Lấy iata nếu là sân bay
                            String iata = null;
                            JSONObject ds = p.optJSONObject("datasource");
                            if (ds != null) {
                                JSONObject raw = ds.optJSONObject("raw");
                                if (raw != null) iata = raw.optString("iata", null);
                            }

                            String resultType = p.optString("result_type", "");
                            String city = p.optString("city", p.optString("name", ""));
                            String country = p.optString("country", "");
                            String code = iata;
                            String label;

                            if (resultType.contains("airport") && iata != null && !iata.isEmpty()) {
                                label = String.format(Locale.US, "%s (%s)", p.optString("name", "Airport"), iata);
                                d.add(label); ia.add(iata);
                            } else if (!city.isEmpty()) {
                                // city → không có iata: trả "CITY" để caller mở city-picker tiếp theo nếu muốn
                                String cityCode = p.optString("citycode", "");
                                label = String.format(Locale.US, "%s, %s%s",
                                        city, country, cityCode.isEmpty() ? "" : (" ("+cityCode+")"));
                                d.add(label);
                                ia.add(cityCode.isEmpty() ? city.toUpperCase(Locale.US) : cityCode);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        display.clear(); iatas.clear();
                        display.addAll(d); iatas.addAll(ia);
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}