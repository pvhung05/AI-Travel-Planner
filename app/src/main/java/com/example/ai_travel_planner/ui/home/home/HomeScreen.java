// app/src/main/java/com/example/ai_travel_planner/ui/home/home/HomeScreen.java
package com.example.ai_travel_planner.ui.home.home;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.ai.GeminiClient;
import com.example.ai_travel_planner.utils.BottomNav;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    private TextView tvGreeting;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private ImageView imgFilter;

    private PlaceAdapter adapter;
    private List<String> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        tvGreeting = findViewById(R.id.tvGreeting);
        recyclerView = findViewById(R.id.recyclerPlaces);
        etSearch = findViewById(R.id.etSearch);
        imgFilter = findViewById(R.id.imgFilter);

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
    }

//    private void openPromptDialog() {
//        View form = getLayoutInflater().inflate(R.layout.dialog_ai_params, null, false);
//        EditText edtDestination = form.findViewById(R.id.edtDestination);
//        EditText edtDays = form.findViewById(R.id.edtDays);
//        EditText edtBudget = form.findViewById(R.id.edtBudget);
//        EditText edtStyle = form.findViewById(R.id.edtStyle);
//
//        edtDestination.setText(etSearch.getText().toString().trim());
//
//        new AlertDialog.Builder(this)
//                .setView(form)
//                .setPositiveButton("Tạo", (d, w) -> {
//                    String dest = edtDestination.getText().toString().trim();
//                    int days = edtDays.getText().toString().isEmpty()
//                            ? 3 : Integer.parseInt(edtDays.getText().toString());
//                    double budget = edtBudget.getText().toString().isEmpty()
//                            ? 3_000_000 : Double.parseDouble(edtBudget.getText().toString());
//                    String style = edtStyle.getText().toString().isEmpty()
//                            ? "tổng hợp" : edtStyle.getText().toString();
//
//                    if (dest.isEmpty()) {
//                        Toast.makeText(this, "Điền điểm đến!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    callAI(dest, days, style, budget);
//                })
//                .setNegativeButton("Huỷ", null)
//                .show();
//    }
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



    /** Rút text từ JSON response của Gemini; nếu không có, trả null */
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
        ProgressDialog progress = ProgressDialog.show(
                this, "Đang sinh lịch trình", "Vui lòng đợi...", true, false);

        new Thread(() -> {
            try {
                String prompt = String.format(
                        "Viết lịch trình DU LỊCH bằng tiếng Việt, %d ngày ở %s, ngân sách khoảng %.0f VND, " +
                                "phong cách %s. Trình bày rõ ràng theo từng ngày, có gợi ý địa điểm, ẩm thực, di chuyển " +
                                "và ước tính chi phí tổng. Nội dung an toàn, thân thiện.",
                        days, destination, budget, style
                );

                String raw = GeminiClient.callGemini(HomeScreen.this, prompt);
                android.util.Log.d("GEMINI_RAW", raw);

                String text = extractTextFromGemini(raw);
                if (text == null || text.isEmpty()) {
                    // Fallback: hiển thị raw JSON nếu không trích được text
                    text = raw;
                }
                String show = sanitizeMarkdown(text);
                String finalShow = show;

                runOnUiThread(() -> {
                    if (progress != null) progress.dismiss();
                    new AlertDialog.Builder(HomeScreen.this)
                            .setTitle("Kế hoạch AI")
                            .setMessage(finalShow.length() > 8000
                                    ? finalShow.substring(0, 8000) + "\n...(truncated)"
                                    : finalShow)
                            .setPositiveButton("OK", null)
                            .show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (progress != null) progress.dismiss();
                    Toast.makeText(HomeScreen.this,
                            "Lỗi AI: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}