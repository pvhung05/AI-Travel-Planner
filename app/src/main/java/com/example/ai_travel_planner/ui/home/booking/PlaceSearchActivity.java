// app/src/main/java/com/example/ai_travel_planner/ui/home/booking/PlaceSearchActivity.java
package com.example.ai_travel_planner.ui.home.booking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.booking.net.GeoService;
import com.example.ai_travel_planner.ui.home.booking.net.GeoapifyResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceSearchActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText edtQuery;
    private RecyclerView rv;
    private PlacesAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);

        btnBack = findViewById(R.id.btnBack);
        edtQuery = findViewById(R.id.edtQuery);
        rv = findViewById(R.id.rvResults);
        rv.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        adapter = new PlacesAdapter(p -> {
            String display = p.formatted;
            if (display == null || display.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                if (p.name != null) sb.append(p.name);
                if (p.city != null) sb.append(sb.length() > 0 ? ", " : "").append(p.city);
                if (p.country != null) sb.append(sb.length() > 0 ? ", " : "").append(p.country);
                display = sb.toString();
            }
            Intent data = new Intent();
            data.putExtra("name", display);
            data.putExtra("formatted", display);
            data.putExtra("lat", p.lat);
            data.putExtra("lng", p.lon);
            setResult(RESULT_OK, data);
            finish();
        });
        rv.setAdapter(adapter);

        edtQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (pending != null) handler.removeCallbacks(pending);
                pending = () -> searchGeoapify(s.toString());
                handler.postDelayed(pending, 350);
            }
        });
    }

    private void searchGeoapify(String q) {
        if (q == null || q.trim().length() < 2) {
            adapter.setData(Collections.emptyList());
            return;
        }
        String apiKey = GeoService.geoKey();
        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(this, "Thiếu GEOAPIFY_KEY", Toast.LENGTH_SHORT).show();
            return;
        }

        // filter có thể để null hoặc "countrycode:vn"
        GeoService.api().autocomplete(q.trim(), 10, apiKey, null, "en")
                .enqueue(new Callback<GeoapifyResponse>() {
                    @Override public void onResponse(Call<GeoapifyResponse> call, Response<GeoapifyResponse> rsp) {
                        if (!rsp.isSuccessful() || rsp.body() == null || rsp.body().features == null) {
                            adapter.setData(Collections.emptyList());
                            return;
                        }
                        List<GeoapifyResponse.Properties> out = new ArrayList<>();
                        for (GeoapifyResponse.Feature f : rsp.body().features) {
                            if (f != null && f.properties != null) out.add(f.properties);
                        }
                        adapter.setData(out);
                    }

                    @Override public void onFailure(Call<GeoapifyResponse> call, Throwable t) {
                        adapter.setData(Collections.emptyList());
                    }
                });
    }
}