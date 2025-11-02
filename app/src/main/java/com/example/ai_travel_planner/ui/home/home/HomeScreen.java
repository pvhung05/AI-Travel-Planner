// app/src/main/java/com/example/ai_travel_planner/ui/home/home/HomeScreen.java
package com.example.ai_travel_planner.ui.home.home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.network.ai.GeminiClient;
import com.example.ai_travel_planner.utils.BottomNav;
import com.example.ai_travel_planner.ui.home.plan.ImageResolver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    private TextView tvGreeting;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private ImageView imgFilter;

    private PlaceAdapter adapter;
    private List<String> places;

    private String lastDestination = null;
    private int    lastDays        = 0;
    private double  lastBudget      = 0;
    private String lastStyle       = null;

    private LinearLayout btnTravel, btnHotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        tvGreeting = findViewById(R.id.tvGreeting);
        recyclerView = findViewById(R.id.recyclerPlaces);
        etSearch = findViewById(R.id.etSearch);
        imgFilter = findViewById(R.id.imgFilter);
        btnHotel = findViewById(R.id.btnHotel);
        btnTravel = findViewById(R.id.btnTravel);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        places = Arrays.asList("Hà Nội", "Đà Nẵng", "TP. Hồ Chí Minh", "Nha Trang", "Phú Quốc");
        adapter = new PlaceAdapter(places);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_home);

        tvGreeting.setText("Hi, Traveler 👋");

        etSearch.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String destination = etSearch.getText().toString().trim();
                if (destination.isEmpty()) {
                    Toast.makeText(this, "Nhập điểm đến trước đã!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                callAI(destination, 3, "tổng hợp", 3_000_000);
                return true;
            }
            return false;
        });

        imgFilter.setOnClickListener(v -> openPromptDialog());
        btnHotel.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, com.example.ai_travel_planner.ui.home.booking.HotelBookingScreen.class);
            startActivity(intent);
        });
    }

    private void openPromptDialog() {
        View form = getLayoutInflater().inflate(R.layout.dialog_ai_params, null, false);

        // Bo góc nền cho layout dialog
        form.setBackgroundResource(R.drawable.bg_dialog);

        // Ánh xạ view
        EditText edtDestination = form.findViewById(R.id.edtDestination);
        EditText edtDays = form.findViewById(R.id.edtDays);
        EditText edtBudget = form.findViewById(R.id.edtBudget);
        EditText edtStyle = form.findViewById(R.id.edtStyle);
        View btnConfirm = form.findViewById(R.id.btnConfirm);
        View btnCancel = form.findViewById(R.id.btnCancel);

        edtDestination.setText(etSearch.getText().toString().trim());

        // Tạo dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(form)
                .setCancelable(true)
                .create();

        // 💡 Xóa khung nền mặc định để hiển thị bo góc thật
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnConfirm.setOnClickListener(v -> {
            String dest = edtDestination.getText().toString().trim();
            String daysStr = edtDays.getText().toString().trim();
            String budgetStr = edtBudget.getText().toString().trim();
            String style = edtStyle.getText().toString().trim();

            if (dest.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập điểm đến!", Toast.LENGTH_SHORT).show();
                return;
            }

            int days = daysStr.isEmpty() ? 3 : Integer.parseInt(daysStr);
            double budget = budgetStr.isEmpty() ? 3_000_000 : Double.parseDouble(budgetStr);
            if (style.isEmpty()) style = "tổng hợp";

            callAI(dest, days, style, budget);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    /**
     * Rút text từ JSON response của Gemini; nếu không có, trả null
     */
    private String extractTextFromGemini(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (root.has("promptFeedback")) {
                JsonObject fb = root.getAsJsonObject("promptFeedback");
                if (fb.has("blockReason")) {
                    return "[Blocked by safety] reason=" + fb.get("blockReason").getAsString();
                }
            }

            if (!root.has("candidates")) return null;
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates.size() == 0) return null;

            StringBuilder sb = new StringBuilder();
            for (JsonElement ce : candidates) {
                JsonObject c = ce.getAsJsonObject();
                if (!c.has("content")) continue;
                JsonObject content = c.getAsJsonObject("content");
                if (!content.has("parts")) continue;
                JsonArray parts = content.getAsJsonArray("parts");
                for (JsonElement pe : parts) {
                    JsonObject p = pe.getAsJsonObject();
                    if (p.has("text")) {
                        sb.append(p.get("text").getAsString()).append("\n");
                    }
                }
            }
            String out = sb.toString().trim();
            return out.isEmpty() ? null : out;

        } catch (Exception e) {
            android.util.Log.e("GEMINI_PARSE", "Parse error: " + e.getMessage(), e);
            return null;
        }
    }

    private String sanitizeMarkdown(String s) {
        if (s == null) return null;
        s = s.replaceAll("```(json|JSON)?", "```");
        s = s.replace("```", "");
        return s.trim();
    }

    private void callAI(String destination, int days, String style, double budget) {

        this.lastDestination = destination;
        this.lastDays        = days;
        this.lastBudget      = budget;
        this.lastStyle       = style;

        // 1️⃣ Tạo Progress Dialog đẹp với bo góc thủ công
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null);

        // Tạo dialog bình thường
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // QUAN TRỌNG: Set background transparent cho window
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        progressDialog.show();

        // Áp dụng bo góc thủ công cho content view
        if (progressDialog.getWindow() != null) {
            View contentView = progressDialog.getWindow().findViewById(android.R.id.content);
            if (contentView != null) {
                ViewGroup parent = (ViewGroup) contentView.getParent();
                if (parent != null) {
                    // Tạo background bo góc
                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.RECTANGLE);
                    shape.setCornerRadius(40f); // Bo góc 40dp
                    shape.setColor(Color.WHITE); // Màu nền trắng

                    // Áp dụng background bo góc
                    parent.setBackground(shape);

                    // Thêm shadow/elevation
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        parent.setElevation(16f);
                    }
                }
            }
        }

        new Thread(() -> {
            try {
                // 2️⃣ Tạo prompt
                String prompt = String.format(
                        "Viết lịch trình DU LỊCH bằng tiếng Việt, %d ngày ở %s, ngân sách khoảng %.0f VND, " +
                                "phong cách %s. Trình bày rõ ràng theo từng ngày, có gợi ý địa điểm, ẩm thực, di chuyển " +
                                "và ước tính chi phí tổng. Nội dung an toàn, thân thiện.",
                        days, destination, budget, style
                );

                // 3️⃣ Gọi Gemini AI
                String raw = GeminiClient.callGemini(HomeScreen.this, prompt);
                android.util.Log.d("GEMINI_RAW", raw);

                String text = extractTextFromGemini(raw);
                if (text == null || text.isEmpty()) {
                    text = raw; // fallback nếu không trích được text
                }
                String show = sanitizeMarkdown(text);
                String finalShow = show;

                // 🔥 IN RA TERMINAL
                android.util.Log.d("FINAL_SHOW", "=== START FINAL SHOW ===");
                android.util.Log.d("FINAL_SHOW", finalShow);
                android.util.Log.d("FINAL_SHOW", "=== END FINAL SHOW ===");

                // 4️⃣ Hiển thị result dialog với layout mới - TRUYỀN THÊM THAM SỐ
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();

                    displayAIContent(finalShow, destination, days, style, budget);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(HomeScreen.this,
                            "Lỗi AI: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void displayAIContent(String finalShow, String destination, int days, String style, double budget) {
        View resultDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ai_result, null);
        LinearLayout containerSections = resultDialogView.findViewById(R.id.containerSections);
        Button btnClose = resultDialogView.findViewById(R.id.btnClose);
        Button btnSave  = resultDialogView.findViewById(R.id.btnSave);
        // CẬP NHẬT PHẦN TỔNG QUAN THEO TRUY VẤN
        updateOverviewSection(resultDialogView, destination, days, style, budget);

        // Phân tích và chia nội dung thành các section
        List<Section> sections = parseAIContent(finalShow);

        // Thêm các section vào container
        for (Section section : sections) {
            View sectionView = LayoutInflater.from(this).inflate(R.layout.layout_section_item, null);
            TextView tvSectionTitle = sectionView.findViewById(R.id.tvSectionTitle);
            TextView tvSectionContent = sectionView.findViewById(R.id.tvSectionContent);

            tvSectionTitle.setText(section.getTitle());
            tvSectionContent.setText(section.getContent());

            containerSections.addView(sectionView);
        }

        AlertDialog resultDialog = new AlertDialog.Builder(this)
                .setView(resultDialogView)
                .setCancelable(true)
                .create();

        if (resultDialog.getWindow() != null) {
            resultDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            new Thread(() -> {
                try {
                    // ✅ lấy url ảnh chuẩn địa danh
                    String imageUrl = ImageResolver.resolve(HomeScreen.this, destination);

                    AppDatabase db = AppDatabase.getInstance(HomeScreen.this);
                    PlanEntity p = new PlanEntity();
                    p.destination = destination;
                    p.days        = days;
                    p.style       = style;
                    p.budget      = budget;
                    p.content     = finalShow;
                    p.createdAt   = System.currentTimeMillis();
                    p.imageUrl    = imageUrl;    // <-- LƯU VÀO DB

                    long id = db.planDao().insert(p);

                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(HomeScreen.this, "Đã lưu (id=" + id + ")", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(HomeScreen.this, "Lỗi lưu: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
        btnClose.setOnClickListener(v -> resultDialog.dismiss());
        resultDialog.show();
    }

    // Phương thức cập nhật phần tổng quan
    private void updateOverviewSection(View dialogView, String destination, int days, String style, double budget) {
        TextView tvDestination = dialogView.findViewById(R.id.tvDestination);
        TextView tvDuration = dialogView.findViewById(R.id.tvDuration);
        TextView tvBudget = dialogView.findViewById(R.id.tvBudget);
        TextView tvStyle = dialogView.findViewById(R.id.tvStyle);

        // Format số tiền
        String formattedBudget = formatBudget(budget);

        // Cập nhật thông tin
        tvDestination.setText(destination);
        tvDuration.setText(days + " ngày " + (days - 1) + " đêm");
        tvBudget.setText("Ngân sách: " + formattedBudget);
        tvStyle.setText("Phong cách: " + style);
    }

    // Phương thức format số tiền
    private String formatBudget(double budget) {
        if (budget >= 1000000) {
            return String.format("%,.1f triệu", budget / 1000000).replace(",", ".");
        } else if (budget >= 1000) {
            return String.format("%,.0f nghìn", budget / 1000).replace(",", ".");
        } else {
            return String.format("%,.0f", budget).replace(",", ".");
        }
    }

    // Class để lưu thông tin section
    private static class Section {
        private String title;
        private String content;

        public Section(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() { return title; }
        public String getContent() { return content; }
    }

    // Phương thức phân tích nội dung AI
    // Phương thức làm sạch markdown
    private String cleanMarkdown(String text) {
        if (text == null) return "";
        return text.replace("**", "")
                .replace("*", "")
                .replace("__", "")
                .replace("_", "")
                .replace("###", "")
                .replace("##", "")
                .replace("#", "")
                .trim();
    }

    // Phương thức phân tích nội dung AI với làm sạch markdown
    private List<Section> parseAIContent(String content) {
        List<Section> sections = new ArrayList<>();

        // Tách nội dung bằng các pattern tiêu đề
        String[] lines = content.split("\n");
        StringBuilder currentContent = new StringBuilder();
        String currentTitle = "Giới thiệu";

        for (String line : lines) {
            line = line.trim();

            // Phát hiện tiêu đề section (dựa trên ** và ###)
            if (line.matches(".*\\*\\*.*\\*\\*.*") || line.matches("###.*") || line.matches(".*---.*")) {

                // Lưu section trước đó nếu có nội dung
                if (currentContent.length() > 0) {
                    String cleanContent = cleanMarkdown(currentContent.toString());
                    sections.add(new Section(currentTitle, cleanContent.trim()));
                    currentContent = new StringBuilder();
                }

                // Xử lý tiêu đề mới với làm sạch markdown
                if (line.matches(".*\\*\\*.*\\*\\*.*")) {
                    currentTitle = cleanMarkdown(line);
                } else if (line.matches("###.*")) {
                    currentTitle = cleanMarkdown(line);
                } else if (line.contains("---")) {
                    currentTitle = "Thông tin quan trọng";
                }

            } else if (!line.isEmpty()) {
                // Làm sạch markdown trước khi thêm vào nội dung
                String cleanLine = cleanMarkdown(line);
                currentContent.append(cleanLine).append("\n\n");
            }
        }

        // Thêm section cuối cùng
        if (currentContent.length() > 0) {
            String cleanContent = cleanMarkdown(currentContent.toString());
            sections.add(new Section(currentTitle, cleanContent.trim()));
        }

        return sections;
    }
}