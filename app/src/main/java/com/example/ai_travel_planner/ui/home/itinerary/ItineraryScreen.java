package com.example.ai_travel_planner.ui.home.itinerary;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.models.AiPlanResponse;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ItineraryScreen extends AppCompatActivity {

    RecyclerView rvDays;
    TextView tvHeader, tvWeather;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_itinerary);

        tvHeader = findViewById(R.id.tvHeader);
        tvWeather = findViewById(R.id.tvWeather);
        rvDays = findViewById(R.id.rvDays);

        // Lấy dữ liệu từ Intent: ưu tiên plan_text, nếu không có thì đọc từ plan_path
        String json = null;
        if (getIntent().hasExtra("plan_text")) {
            json = getIntent().getStringExtra("plan_text");
        } else if (getIntent().hasExtra("plan_path")) {
            String path = getIntent().getStringExtra("plan_path");
            json = readFileAll(path);
        }

        if (json == null || json.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu lịch trình", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Cố gắng parse sang schema AiPlanResponse
        try {
            AiPlanResponse plan = gson.fromJson(json, AiPlanResponse.class);

            tvHeader.setText(plan.destination + " • " + plan.days + " days • ~" +
                    (long) plan.estimatedTotalCost + " " + plan.currency);

            if (plan.weather != null) {
                String tips = (plan.weather.tips != null && !plan.weather.tips.isEmpty())
                        ? ("Tips: " + String.join(", ", plan.weather.tips))
                        : "";
                tvWeather.setText(plan.weather.period + " • " + plan.weather.overview + (tips.isEmpty() ? "" : ("\n" + tips)));
            } else {
                tvWeather.setText("");
            }

            rvDays.setLayoutManager(new LinearLayoutManager(this));
            rvDays.setAdapter(new com.example.ai_travel_planner.ui.home.itinerary.ItineraryAdapter(plan.itinerary));

        } catch (Exception ex) {
            // Nếu không đúng schema (AI trả text tự do) -> show raw để debug, không crash
            tvHeader.setText("AI Plan (raw)");
            tvWeather.setText(json);
            rvDays.setVisibility(View.GONE);
        }
    }

    private String readFileAll(String absPath) {
        try (FileInputStream fis = new FileInputStream(absPath);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String ln;
            while ((ln = br.readLine()) != null) {
                sb.append(ln).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}