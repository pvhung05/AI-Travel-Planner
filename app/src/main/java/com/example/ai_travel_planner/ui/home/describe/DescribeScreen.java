package com.example.ai_travel_planner.ui.home.describe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.WeatherApiClient;
import com.example.ai_travel_planner.network.WeatherApiService;
import com.example.ai_travel_planner.network.ai.GeminiClient;
import com.example.ai_travel_planner.network.models.WeatherResponse;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;
import com.example.ai_travel_planner.ui.home.plan.PlanScreen;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DescribeScreen extends Activity {
    ImageButton btnBack, btnBookmark;
    Button btnEnterPlan, btnViewOther;

    TextView tvDescription, tvTemp;

    ApiService apiService;

    WeatherApiService weatherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.describescreen);

        apiService = ApiClient.getClient().create(ApiService.class);
        weatherService = WeatherApiClient.getClient().create(WeatherApiService.class);

        ImageView ivPlace = findViewById(R.id.ivPlace);
        String imageUrl = getIntent().getStringExtra("PLACE_IMAGE_URL");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image) // Ảnh chờ
                    .error(R.drawable.error_image)         // Ảnh lỗi
                    .into(ivPlace);
        } else {
            ivPlace.setImageResource(R.drawable.default_image); // Ảnh mặc định
        }

        Double rating = getIntent().getDoubleExtra("placeRating", 0.0);
        TextView tvRating = findViewById(R.id.tvPlaceRating);

        if (rating != 0.0) {
            tvRating.setText(String.valueOf(rating));
        } else {
            tvRating.setText("N/A");
        }

        String placeName = getIntent().getStringExtra("placeName");
        TextView tvTitle = findViewById(R.id.tvPlaceName);
        tvTitle.setText(placeName);

        tvDescription = findViewById(R.id.tvDescription);
        String description = getIntent().getStringExtra("PLACE_DESCRIPTION");

        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
        } else {
            fetchDescriptionFromAI(placeName);
        }

        tvTemp = findViewById(R.id.tvTemp);
        double latitude = getIntent().getDoubleExtra("PLACE_LATITUDE", 0);
        double longitude = getIntent().getDoubleExtra("PLACE_LONGITUDE", 0);

        if (latitude != 0 && longitude != 0) {
            fetchWeather(latitude, longitude);
        } else {
            Log.e("WeatherAPI", "Không có tọa độ cho địa điểm này");
            tvTemp.setText("N/A"); // Không có tọa độ
        }

        btnBack = findViewById(R.id.btnBack);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnEnterPlan = findViewById(R.id.btnEnterPlan);
        btnViewOther = findViewById(R.id.btnViewOther);

        // Nút back chỉ cần finish() để quay lại màn hình trước
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEnterPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DescribeScreen.this, PlanScreen.class);
                startActivity(intent);
                finish();
            }
        });

        btnViewOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DescribeScreen.this, HomeScreen.class);
                startActivity(intent);
                finish();
            }
        });

//        btnBookmark.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        finish(); // Quay lại màn hình trước đó
    }

    private void fetchDescriptionFromAI(String placeName) {
        // Hiển thị loading
        tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText("Đang tải mô tả ...");

        new Thread(() -> {
            try {
                // Tạo prompt
                String prompt = String.format(
                        "Viết một đoạn mô tả tổng quan (overview) ngắn gọn (khoảng 3-4 câu) bằng tiếng Việt cho địa điểm du lịch: %s",
                        placeName
                );

                // Gọi Gemini AI
                String raw = GeminiClient.callGemini(DescribeScreen.this, prompt);
                android.util.Log.d("GEMINI_DESC_RAW", raw);

                String text = extractTextFromGemini(raw);
                if (text == null || text.isEmpty()) {
                    text = "Không thể tạo mô tả cho địa điểm này.";
                }
                String finalDescription = sanitizeMarkdown(text);

                // Hiển thị kết quả lên UI Thread
                runOnUiThread(() -> {
                    tvDescription.setText(finalDescription);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvDescription.setText("Lỗi khi tải mô tả: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Rút text từ JSON response của Gemini
     */
    private String extractTextFromGemini(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("candidates")) {
                JsonArray candidates = root.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                    if (content.has("parts")) {
                        JsonArray parts = content.getAsJsonArray("parts");
                        if (parts.size() > 0 && parts.get(0).getAsJsonObject().has("text")) {
                            return parts.get(0).getAsJsonObject().get("text").getAsString();
                        }
                    }
                }
            }
            if (root.has("promptFeedback") && root.getAsJsonObject("promptFeedback").has("blockReason")) {
                return "[Blocked: " + root.getAsJsonObject("promptFeedback").get("blockReason").getAsString() + "]";
            }
            return null;
        } catch (Exception e) {
            android.util.Log.e("GEMINI_PARSE", "Parse error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Làm sạch markdown
     */
    private String sanitizeMarkdown(String s) {
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

    private void fetchWeather(double lat, double lon) {
        Log.d("WeatherAPI", "Đang tải thời tiết cho: " + lat + ", " + lon);
        tvTemp.setText("... °C");
        String api_key = getString(R.string.weather_api_key);

        weatherService.getCurrentWeather(lat, lon, api_key, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().main != null) {
                    double temp = response.body().main.temp;
                    tvTemp.setText(String.format("%.0f °C", temp));
                    Log.d("WeatherAPI", "Nhiệt độ: " + temp);
                } else {
                    Log.e("WeatherAPI", "Lỗi tải thời tiết: " + response.code());
                    tvTemp.setText("N/A");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherAPI", "Lỗi mạng (thời tiết)", t);
                tvTemp.setText("N/A"); // Lỗi mạng
            }
        });
    }
}