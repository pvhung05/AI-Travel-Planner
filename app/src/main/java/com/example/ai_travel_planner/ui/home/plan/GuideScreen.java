package com.example.ai_travel_planner.ui.home.plan;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.ui.home.plan.ImageResolver; // ✅ dùng utils

public class GuideScreen extends AppCompatActivity {

    ImageView imgHero;
    ImageButton btnBack;
    TextView txtTitle, txtAbout, txtTravelTitle, txtTravelSub, txtHotelTitle, txtHotelSub, txtRating, labelAbout;

    long planId = -1;
    PlanEntity plan; // dữ liệu lấy từ DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guidescreen);

        imgHero       = findViewById(R.id.imgHero);
        btnBack       = findViewById(R.id.btnBack);
        txtTitle      = findViewById(R.id.txtTitle);
        txtAbout      = findViewById(R.id.txtAbout);
        txtTravelTitle= findViewById(R.id.txtTravelTitle);
        txtTravelSub  = findViewById(R.id.txtTravelSub);
        txtHotelTitle = findViewById(R.id.txtHotelTitle);
        txtHotelSub   = findViewById(R.id.txtHotelSub);
        txtRating     = findViewById(R.id.txtRating);
        labelAbout    = findViewById(R.id.labelAbout);

        planId = getIntent().getLongExtra("plan_id", -1);
        if (planId <= 0) {
            Toast.makeText(this, "Thiếu plan_id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());

        // Load dữ liệu từ DB
        new Thread(() -> {
            plan = AppDatabase.getInstance(this).planDao().getById(planId);
            runOnUiThread(() -> bindPlan(plan));
        }).start();
    }

    private void bindPlan(PlanEntity p) {
        if (p == null) {
            Toast.makeText(this, "Không tìm thấy kế hoạch", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Tiêu đề
        txtTitle.setText(p.destination);

        // Ảnh hero → ƯU TIÊN dùng ảnh đã lưu
        String heroUrl = (p.imageUrl != null && !p.imageUrl.isEmpty())
                ? p.imageUrl
                : ImageResolver.buildFallbackUrl(p.destination);

        Glide.with(this)
                .load(heroUrl)
                .placeholder(R.drawable.sample_mountain)
                .centerCrop()
                .into(imgHero);

        if (heroUrl == null) {
            // Fallback: resolve ảnh theo địa danh, rồi update DB để lần sau ổn định
            heroUrl = ImageResolver.buildFallbackUrl(p.destination);

            final String finalUrl = heroUrl;
            new Thread(() -> {
                try {
                    AppDatabase.getInstance(GuideScreen.this)
                            .planDao().updateImage(p.id, finalUrl);
                    // Cập nhật object hiện tại để đồng bộ
                    p.imageUrl = finalUrl;
                } catch (Exception ignore) {}
            }).start();
        }

        Glide.with(this)
                .load(heroUrl)
                .placeholder(R.drawable.sample_mountain)
                .error(R.drawable.sample_mountain)
                .centerCrop()
                .into(imgHero);

        // Mục "About": lấy phần mở đầu từ content AI (nếu có)
        String about = extractAbout(p.content);
        labelAbout.setText("About " + p.destination);
        if (about == null || about.trim().isEmpty()) {
            txtAbout.setText("Chưa có mô tả. Đây là lịch trình được sinh từ AI.");
        } else {
            txtAbout.setText(about);
        }

        // Rating: đặt tạm giá trị mô phỏng
        txtRating.setText("4.7 (128 reviews)");

        // Pricing (demo): tuỳ chỉnh theo nhu cầu
        txtTravelTitle.setText("Traveling");
        txtTravelSub.setText("from ~ " + formatMoney(p.budget * 0.3) + " VND");
        txtHotelTitle.setText("Hotels");
        txtHotelSub.setText("from ~ " + formatMoney(p.budget * 0.2) + " VND");
    }

    private String extractAbout(String content) {
        if (content == null) return null;
        // Lấy đoạn đầu tiên trước khi đến các dòng "Day 1/2/3..."
        String[] lines = content.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String ln : lines) {
            String t = ln.trim();
            if (t.matches("(?i)^day\\s*\\d+.*")) {
                break; // dừng khi gặp block Day
            }
            if (!t.isEmpty()) {
                sb.append(t).append("\n");
            }
            if (sb.length() > 600) break;
        }
        return sb.toString().trim();
    }

    private String formatMoney(double v) {
        if (v >= 1_000_000) {
            return String.format("%,.1f triệu", v / 1_000_000).replace(",", ".");
        }
        if (v >= 1_000) {
            return String.format("%,.0f nghìn", v / 1_000).replace(",", ".");
        }
        return String.format("%,.0f", v).replace(",", ".");
    }
}