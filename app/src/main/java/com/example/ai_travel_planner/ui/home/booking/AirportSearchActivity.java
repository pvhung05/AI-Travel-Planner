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
import com.example.ai_travel_planner.net.amadeus.AmadeusClient;
import com.example.ai_travel_planner.net.amadeus.AmadeusService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Retrofit;

public class AirportSearchActivity extends AppCompatActivity {

    public static final String EXTRA_IATA  = "extra_iata";
    public static final String EXTRA_LABEL = "extra_label";

    private EditText txtSearch;
    private ListView lvResults;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> display = new ArrayList<>();
    private final ArrayList<String> iatas   = new ArrayList<>();

    private final OkHttpClient http = new OkHttpClient();

    private static final String FALLBACK_GEO_KEY = "5316284bf01d48039b1fdc517f3160a8";

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
                                String code1 = iata.toUpperCase(Locale.US);
                                if (code1.matches("[A-Z]{3}")) {
                                    label = String.format(Locale.US, "%s (%s)", p.optString("name", "Airport"), code1);
                                    d.add(label); ia.add(code1);
                                }
                            } else if (!city.isEmpty()) {
                                // Chỉ chấp nhận city có citycode là IATA 3 ký tự
                                String cityCode = p.optString("citycode", "").toUpperCase(Locale.US);
                                if (cityCode.matches("[A-Z]{3}")) {
                                    label = String.format(Locale.US, "%s, %s (%s)", city, country, cityCode);
                                    d.add(label);
                                    ia.add(cityCode);
                                }
                            }
                        }
                    }

                    // Fallback: nếu chưa có gợi ý hợp lệ, gọi Amadeus Locations
                    if (ia.isEmpty()) {
                        try {
                            Retrofit rt = AmadeusClient.authed();
                            AmadeusService svc = rt.create(AmadeusService.class);
                            Call<com.google.gson.JsonObject> call = svc.searchLocations("AIRPORT,CITY", q, 8, "analytics.travelers.score");
                            retrofit2.Response<com.google.gson.JsonObject> r2 = call.execute();
                            if (r2.isSuccessful() && r2.body() != null) {
                                JsonArray arr = r2.body().has("data") && r2.body().get("data").isJsonArray()
                                        ? r2.body().getAsJsonArray("data") : new JsonArray();
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject o = el.getAsJsonObject();
                                    String code = o.has("iataCode") ? o.get("iataCode").getAsString() : "";
                                    if (code == null) code = "";
                                    code = code.trim().toUpperCase(java.util.Locale.US);
                                    if (!code.matches("[A-Z]{3}")) continue;
                                    String name = o.has("name") ? o.get("name").getAsString() : code;
                                    String country = "";
                                    if (o.has("address") && o.get("address").isJsonObject()) {
                                        JsonObject addr = o.getAsJsonObject("address");
                                        if (addr.has("countryName")) country = addr.get("countryName").getAsString();
                                    }
                                    String label = country.isEmpty() ? (name + " (" + code + ")") : (name + " (" + code + ") - " + country);
                                    // tránh trùng theo IATA
                                    if (!ia.contains(code)) { d.add(label); ia.add(code); }
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    // Bổ sung ưu tiên cho các sân bay Việt Nam quen thuộc & match chính xác mã IATA
                    try {
                        String qUpper = q.trim().toUpperCase(java.util.Locale.US);
                        // inject mapping VN phổ biến
                        java.util.LinkedHashMap<String, String> vn = new java.util.LinkedHashMap<>();
                        vn.put("HAN", "Hanoi (HAN) - Vietnam");
                        vn.put("SGN", "Ho Chi Minh City (SGN) - Vietnam");
                        vn.put("DAD", "Da Nang (DAD) - Vietnam");
                        vn.put("CXR", "Nha Trang Cam Ranh (CXR) - Vietnam");
                        vn.put("DLI", "Da Lat Lien Khuong (DLI) - Vietnam");
                        vn.put("PQC", "Phu Quoc (PQC) - Vietnam");
                        vn.put("HUI", "Hue Phu Bai (HUI) - Vietnam");
                        vn.put("HPH", "Hai Phong Cat Bi (HPH) - Vietnam");
                        vn.put("UIH", "Quy Nhon Phu Cat (UIH) - Vietnam");
                        vn.put("VCA", "Can Tho (VCA) - Vietnam");

                        // Nếu người dùng gõ đúng IATA 3 chữ, đẩy mã đó lên đầu danh sách
                        if (qUpper.matches("[A-Z]{3}")) {
                            if (vn.containsKey(qUpper) && !ia.contains(qUpper)) {
                                d.add(0, vn.get(qUpper));
                                ia.add(0, qUpper);
                            }
                        } else {
                            // Nếu người dùng gõ tên thành phố, thêm các sân bay VN phù hợp tên
                            if (qUpper.contains("HANOI") && !ia.contains("HAN")) { d.add(0, vn.get("HAN")); ia.add(0, "HAN"); }
                            if ((qUpper.contains("HO CHI") || qUpper.contains("SAIGON") || qUpper.contains("HCM")) && !ia.contains("SGN")) { d.add(vn.get("SGN")); ia.add("SGN"); }
                            if (qUpper.contains("DANANG") || qUpper.contains("DA NANG")) { if (!ia.contains("DAD")) { d.add(vn.get("DAD")); ia.add("DAD"); } }
                        }
                    } catch (Exception ignored) {}

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