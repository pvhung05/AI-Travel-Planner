package com.example.ai_travel_planner.ui.home.plan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.ui.home.itinerary.AiPlanRepository;
import com.example.ai_travel_planner.ui.home.itinerary.ItineraryScreen;
import com.example.ai_travel_planner.network.ai.GeminiClient;
import com.example.ai_travel_planner.ui.home.plan.ImageResolver; // ✅ dùng utils

public class GuideScreen extends AppCompatActivity {

    ImageView imgHero;
    ImageButton btnBack;
    Button btnPlanTrip;

    private PlanEntity plan; // model bạn đang dùng ở GuideScreen
    private String mDestination;
    private int mDays;
    private double mBudget;
    private String mStyle;
    TextView txtTitle, txtAbout, txtTravelTitle, txtTravelSub, txtHotelTitle, txtHotelSub, txtRating, labelAbout;

    long planId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guidescreen);
        loadPlanFromIntentOrLatest();

        imgHero = findViewById(R.id.imgHero);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtAbout = findViewById(R.id.txtAbout);
        txtTravelTitle = findViewById(R.id.txtTravelTitle);
        txtTravelSub = findViewById(R.id.txtTravelSub);
        txtHotelTitle = findViewById(R.id.txtHotelTitle);
        txtHotelSub = findViewById(R.id.txtHotelSub);
        txtRating = findViewById(R.id.txtRating);
        labelAbout = findViewById(R.id.labelAbout);
        btnPlanTrip = findViewById(R.id.btnPlanTrip);


        planId = getIntent().getLongExtra("plan_id", -1);
        if (planId <= 0) {
            Toast.makeText(this, "Thiếu plan_id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnPlanTrip.setOnClickListener(v -> goPlanTrip(v));
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
                } catch (Exception ignore) {
                }
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

    private void loadPlanFromIntentOrLatest() {
        final AiPlanRepository repo = AiPlanRepository.getInstance(getApplicationContext());

        final long planIdLong = getIntent().getLongExtra("id", -1L);
        // ưu tiên load theo id, nếu null thì lấy bản gần nhất
        new Thread(() -> {
            PlanEntity p = null;
            try {
                if (planIdLong != -1L) {
                    // nếu bạn dùng id long
                    // => cần có overload getPlanByIdSync(long) trong repo, hoặc tự đổi về String
                    p = repo.getPlanByIdSync(planIdLong);
                } else {
                    p = repo.getLatestPlanSync();
                }
            } catch (Throwable ignored) {
            }

            PlanEntity finalP = p;
            runOnUiThread(() -> {
                if (finalP == null) {
                    Toast.makeText(this, "Không tìm thấy kế hoạch. Hãy lưu plan ở Home trước.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Gán state cho GuideScreen
                this.plan = finalP;
                this.mDestination = finalP.destination;
                this.mDays = finalP.days;
                this.mBudget = (long) finalP.budget;
                this.mStyle = finalP.style;
            });
        }).start();
    }

    private String extractJson(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        // Ưu tiên block {} (object)
        int i = s.indexOf('{');
        int j = s.lastIndexOf('}');
        if (i >= 0 && j > i) return s.substring(i, j + 1);
        // Nếu là mảng []
        int a = s.indexOf('[');
        int b = s.lastIndexOf(']');
        if (a >= 0 && b > a) return s.substring(a, b + 1);
        // Không tìm thấy thì trả lại nguyên văn (để debug ở ItineraryScreen)
        return s;
    }

    public void goPlanTrip(View v) {
        final String dest = (mDestination == null) ? "" : mDestination;
        final int days = (mDays <= 0) ? 0 : mDays;
        final double budget = (mBudget <= 0) ? 0 : mBudget;
        final String style = (mStyle == null) ? "" : mStyle;

        AiPlanRepository repo = AiPlanRepository.getInstance(getApplicationContext());

        AiPlanRepository.LoadCallback open = savedPlan -> {
            if (savedPlan == null || savedPlan.days == null || savedPlan.days.isEmpty()) {
                // Không có bản lưu -> sinh mới luôn
                generateWithAI(v, dest, days, budget, style);  // gọi flow sinh lịch trình hiện tại của bạn ở GuideScreen
                return;
            }
            new androidx.appcompat.app.AlertDialog.Builder(GuideScreen.this)
                    .setTitle("Use saved plan?")
                    .setMessage("Open your last saved plan for " + (dest.isEmpty() ? "this trip" : dest) + " or generate a new one?")
                    .setPositiveButton("Use saved", (d, w) -> openItineraryWithPlan(savedPlan))
                    .setNegativeButton("Generate new", (d, w) -> generateWithAI(v, dest, days, budget, style))
                    .show();
        };

        if (!dest.isEmpty() && days > 0 && !style.isEmpty()) {
            repo.getLatestPlanByMeta(dest, days, style, open);
        } else {
            repo.getLatestPlan(open);
        }

    }

    public void generateWithAI(View v, String mDestination, int mDays, double mBudget, String mStyle) {
        if (mDestination == null || mDestination.trim().isEmpty()
                || mDays <= 0 || mBudget <= 0
                || mStyle == null || mStyle.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu destination/days/budget/style.", Toast.LENGTH_LONG).show();
            return;
        }

        v.setEnabled(false);
        Toast.makeText(this, "Đang sinh lịch trình bằng AI...", Toast.LENGTH_SHORT).show();

        final String apiKey = getString(R.string.gemini_api_key);
        final String systemPrompt =
                "You are a travel planner. Return ONLY valid JSON (no markdown, no commentary).\n" +
                        "{ \"city\":\"string\",\"currency\":\"VND\",\"days\":[{\"label\":\"Day 1\",\"date\":\"YYYY-MM-DD\"," +
                        "\"activities\":[{\"time\":\"HH:mm\",\"title\":\"string\",\"category\":\"food|nature|culture|shopping|adventure|other\"," +
                        "\"location\":\"string|null\",\"costVND\":0,\"notes\":\"string|null\",\"emoji\":\"string|null\"}]}]}";
        final String userPrompt =
                "Destination: " + mDestination + "\n" +
                        "Budget (VND): " + mBudget + "\n" +
                        "Days: " + mDays + "\n" +
                        "Style: " + mStyle + "\n" +
                        "IMPORTANT: Reply with JSON ONLY. Times must be HH:mm. Include a date per day.";

        new Thread(() -> {
            try {
                com.example.ai_travel_planner.network.ai.GeminiGateway gw =
                        new com.example.ai_travel_planner.network.ai.GeminiGateway(apiKey, "gemini-1.5-flash");
                String raw = gw.callGemini(systemPrompt, userPrompt).get();
                String json = extractJson(raw);

                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.example.ai_travel_planner.network.models.TripPlan tp =
                        gson.fromJson(json, com.example.ai_travel_planner.network.models.TripPlan.class);

                runOnUiThread(() -> {
                    v.setEnabled(true);
                    if (tp == null || tp.days == null || tp.days.isEmpty()) {
                        Toast.makeText(this, "AI trả về rỗng/sai schema. Thử lại.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent i = new Intent(this, com.example.ai_travel_planner.ui.home.itinerary.ItineraryScreen.class);
                    i.putExtra("plan_text", json); // Fallback
                    i.putExtra("tripPlan", tp);    // Serializable nếu TripPlan implements
                    startActivity(i);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    v.setEnabled(true);
                    Toast.makeText(this, "Lỗi tạo lịch trình: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public void openItineraryWithPlan(com.example.ai_travel_planner.network.models.TripPlan plan) {
        Intent i = new Intent(GuideScreen.this,
                com.example.ai_travel_planner.ui.home.itinerary.ItineraryScreen.class);
        i.putExtra("tripPlan", plan);
        startActivity(i);
    }
}