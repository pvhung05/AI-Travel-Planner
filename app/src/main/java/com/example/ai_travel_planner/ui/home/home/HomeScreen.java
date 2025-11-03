// app/src/main/java/com/example/ai_travel_planner/ui/home/home/HomeScreen.java
package com.example.ai_travel_planner.ui.home.home;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.ai.GeminiClient;
import com.example.ai_travel_planner.network.models.GeoClient;
import com.example.ai_travel_planner.network.models.GeoRequest;
import com.example.ai_travel_planner.network.models.GeoResponse;
import com.example.ai_travel_planner.utils.BottomNav;
import com.example.ai_travel_planner.ui.home.plan.ImageResolver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.example.ai_travel_planner.ui.home.plan.PlaceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreen extends AppCompatActivity {

    private TextView tvGreeting;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private ImageView imgFilter;

    private PlaceAdapter adapter; // Adapter dùng PlaceItem
    private List<PlaceItem> placeList = new ArrayList<>(); // Danh sách chứa PlaceItem
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private ExecutorService executorService = Executors.newFixedThreadPool(7); // Tăng thread pool để load song song

    private String lastDestination = null;
    private int    lastDays        = 0;
    private double  lastBudget      = 0;
    private String lastStyle       = null;

    private LinearLayout btnTravel, btnHotel;

    private Button btnMostViewed, btnNearby, btnLatest;

    private ApiService apiService;

    private boolean isLoading = false; // Thêm biến cờ để tránh gọi API trùng lặp


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        apiService = ApiClient.getClient().create(ApiService.class);

        tvGreeting = findViewById(R.id.tvGreeting);
        recyclerView = findViewById(R.id.recyclerPlaces);
        etSearch = findViewById(R.id.etSearch);
        imgFilter = findViewById(R.id.imgFilter);
        btnHotel = findViewById(R.id.btnHotel);
        btnTravel = findViewById(R.id.btnTravel);
        btnMostViewed = findViewById(R.id.btnMostViewed);
        btnNearby = findViewById(R.id.btnNearby);
        btnLatest = findViewById(R.id.btnLatest);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PlaceAdapter(this, placeList); // Khởi tạo với Context và List<PlaceItem> rỗng
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_home);

        setActiveButton(btnMostViewed);
        fetchMostViewedPlaces();

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

        btnMostViewed.setOnClickListener(v -> {
            setActiveButton(btnMostViewed);
            fetchMostViewedPlaces();
        });

        btnNearby.setOnClickListener(v -> {
            setActiveButton(btnNearby);
            requestLocationPermission(); // Gọi hàm kiểm tra quyền
        });
        btnTravel.setOnClickListener(v -> {
            Intent it = new Intent(HomeScreen.this, com.example.ai_travel_planner.ui.home.booking.PlaneBookingScreen.class);
            startActivity(it); // hoặc planeLauncher.launch(it) nếu bạn giữ ActivityResult
        });
        btnLatest.setOnClickListener(v -> {
            setActiveButton(btnLatest);
            fetchLatestPlaces();
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

        // Nút Lưu
        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            new Thread(() -> {
                try {
                    // ✅ Lấy URL ảnh chuẩn địa danh
                    String imageUrl = ImageResolver.resolve(HomeScreen.this, destination);

                    AppDatabase db = AppDatabase.getInstance(HomeScreen.this);
                    PlanEntity p = new PlanEntity();
                    p.destination = destination;
                    p.days        = days;
                    p.style       = style;
                    p.budget      = budget;
                    p.content     = finalShow;
                    p.createdAt   = System.currentTimeMillis();
                    p.imageUrl    = imageUrl;

                    long id = db.planDao().insert(p);

                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(HomeScreen.this, "Đã lưu (id=" + id + ")", Toast.LENGTH_SHORT).show();
                        resultDialog.dismiss(); // 🔹 Đóng dialog sau khi lưu thành công
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(HomeScreen.this, "Lỗi lưu: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        // Nút Đóng
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

    private void setActiveButton(Button activeButton) {
        btnMostViewed.setTextColor(getResources().getColor(R.color.button_inactive_text));
        btnNearby.setTextColor(getResources().getColor(R.color.button_inactive_text));
        btnLatest.setTextColor(getResources().getColor(R.color.button_inactive_text));

        activeButton.setTextColor(getResources().getColor(R.color.button_active_text));
    }

    private void requestLocationPermission() {
        Log.d("NearbyFlow", "Step 1: requestLocationPermission called"); // Log bước 1
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("NearbyFlow", "Permission already granted. Proceeding to get location.");
            getCurrentLocationAndFetchPlacesGeo(); // Đã có quyền, lấy vị trí
        } else {
            Log.d("NearbyFlow", "Permission not granted. Requesting permission...");
            ActivityCompat.requestPermissions(this, // Chưa có quyền, yêu cầu
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("NearbyFlow", "Step 2: onRequestPermissionsResult called. RequestCode: " + requestCode); // Log bước 2
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NearbyFlow", "Permission GRANTED. Proceeding to get location.");
                getCurrentLocationAndFetchPlacesGeo(); // Quyền được cấp, lấy vị trí
            } else {
                Log.d("NearbyFlow", "Permission DENIED.");
                Toast.makeText(this, "Quyền vị trí bị từ chối.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocationAndFetchPlacesGeo() {
        Log.d("NearbyFlow", "Step 3: getCurrentLocationAndFetchPlacesGeo called"); // Log bước 3
        Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();
        // Kiểm tra quyền lần nữa (bắt buộc)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NearbyFlow", "Still missing location permission!");
            Toast.makeText(this, "Lỗi: Vẫn thiếu quyền vị trí!", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d("NearbyFlow", "Location obtained: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude());
//                        fetchNearbyPlacesGeoapify(location.getLatitude(), location.getLongitude());
//                        Latitude: 21.038132 ha noi
//                        Longitude: 105.781097
                        fetchNearbyPlacesGeoapify(21.038132, 105.781097);
                    } else {
                        Log.w("NearbyFlow", "getLastLocation returned null.");
                        Toast.makeText(this, "Không thể lấy vị trí. Bật GPS?", Toast.LENGTH_LONG).show();
                        placeList.clear();
                        adapter.updateData(placeList);
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e("LOCATION_ERROR", "Lỗi khi lấy vị trí: ", e);
                    Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    placeList.clear();
                    adapter.updateData(placeList);
                });
    }

    private void fetchNearbyPlacesGeoapify(double lat, double lng) {
        Log.d("NearbyFlow", "Step 4: fetchNearbyPlacesGeoapify called with Lat=" + lat + ", Lng=" + lng); // Log bước 4
        Toast.makeText(this, "Đang tìm địa điểm gần đây (Geoapify)...", Toast.LENGTH_SHORT).show();

        GeoRequest service = GeoClient.getInstance();
        String filter = String.format("circle:%f,%f,20000", lng, lat);
        String bias = String.format("proximity:%f,%f", lng, lat);
        String categories = "building.tourism,building.historic,building.place_of_worship,beach.beach_resort,activity.sport_club,building.entertainment";
        String apiKey = getString(R.string.geoapify_api_key);
        String properties = "rating";

        Log.d("GEOAPIFY_REQUEST", "Filter: " + filter + ", Bias: " + bias + ", Categories: " + categories + ", Properties: " + properties); // Log request params

        retrofit2.Call<GeoResponse> call = service.getNearbyPlaces(categories, filter, bias, 10, apiKey, properties);

        call.enqueue(new retrofit2.Callback<GeoResponse>() {
            @Override
            public void onResponse(retrofit2.Call<GeoResponse> call,
                                   retrofit2.Response<GeoResponse> response) {
                Log.d("NearbyFlow", "Step 5: onResponse Geoapify. Code: " + response.code()); // Log bước 5
                if (response.isSuccessful() && response.body() != null && response.body().features != null) {
                    Log.d("GEOAPIFY_SUCCESS", "Received " + response.body().features.size() + " features.");
                    processGeoapifyResults(response.body().features); // Gọi hàm xử lý kết quả
                } else {
                    String errorMsg = "Lỗi Geoapify API: " + response.code();
                    try {
                        if (response.errorBody() != null)
                            errorMsg += " - " + response.errorBody().string();
                    } catch (Exception e) {
                        Log.e("GEOAPIFY_ERROR", "Error reading error body", e);
                    }
                    Log.e("GEOAPIFY_ERROR", errorMsg);
                    Toast.makeText(HomeScreen.this, errorMsg, Toast.LENGTH_LONG).show();
                    placeList.clear();
                    adapter.updateData(placeList);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GeoResponse> call, Throwable t) {
                Log.e("GEOAPIFY_FAIL", "Lỗi mạng", t);
                Toast.makeText(HomeScreen.this, "Lỗi mạng Geoapify: " + t.getMessage(), Toast.LENGTH_LONG).show();
                placeList.clear();
                adapter.updateData(placeList);
            }
        });
    }

    private void processGeoapifyResults(List<GeoResponse.Feature> features) {
        Log.d("NearbyFlow", "Step 6: processGeoapifyResults called with " + (features != null ? features.size() : 0) + " features."); // Log bước 6

        if (features == null || features.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy địa điểm nào!", Toast.LENGTH_SHORT).show();
            placeList.clear();
            adapter.updateData(placeList);
            return;
        }

        // Tạo danh sách mới và hiển thị ngay lập tức (không có ảnh)
        List<PlaceItem> newList = new ArrayList<>();
        for (GeoResponse.Feature f : features) {
            if (f.properties == null || f.properties.name == null || f.properties.name.isEmpty()) {
                Log.w("ProcessResults", "Skipping feature with null properties or name.");
                continue;
            }

            PlaceItem item = new PlaceItem();
            item.setName(f.properties.name);
            item.setVicinity(f.properties.address_line2 != null ? f.properties.address_line2 :
                    (f.properties.city != null ? f.properties.city : ""));

            if (f.properties != null && f.properties.rating != null) {
                // Geoapify rating (0-10), chia 2 để ra thang 0-5
                item.setRating(f.properties.rating / 2.0);
            } else {
                item.setRating(0.0); // Không có rating
            }

            if (f.geometry != null && f.geometry.coordinates != null && f.geometry.coordinates.size() >= 2) {
                item.setLongitude(f.geometry.coordinates.get(0)); // x = lon
                item.setLatitude(f.geometry.coordinates.get(1));  // y = lat
            } else {
                Log.w("ProcessResults", "Feature missing coordinates: " + f.properties.name);
            }

            item.setImageUrl(null); // Ban đầu không có ảnh
            newList.add(item);
        }

        //  Cập nhật UI ngay lập tức để hiển thị danh sách
        runOnUiThread(() -> {
            Log.d("NearbyFlow", "Step 7: Initial update - Displaying " + newList.size() + " items without images.");
            adapter.updateData(newList);
            Toast.makeText(HomeScreen.this, "Tìm thấy " + newList.size() + " địa điểm!", Toast.LENGTH_SHORT).show();
        });

        // Load ảnh bất đồng bộ cho từng item (progressive loading)
        AtomicInteger completedCount = new AtomicInteger(0);
        int totalItems = newList.size();

        for (int i = 0; i < newList.size(); i++) {
            final int index = i;
            final PlaceItem item = newList.get(index);

            executorService.submit(() -> {
                String imageUrl = null;
                try {
                    Log.d("ImageResolverTask", "Loading image for: " + item.getName());
                    imageUrl = ImageResolver.resolve(HomeScreen.this, item.getName());
                    Log.d("ImageResolverTask", "Resolved image for " + item.getName() + ": " + (imageUrl != null ? "Found" : "Not Found"));
                } catch (Exception e) {
                    Log.e("ImageResolver", "Lỗi lấy ảnh cho " + item.getName(), e);
                }

                final String finalImageUrl = imageUrl;

                // Cập nhật từng item ngay khi load xong ảnh
                runOnUiThread(() -> {
                    if (finalImageUrl != null) {
                        item.setImageUrl(finalImageUrl);
                        adapter.notifyItemChanged(index); // Chỉ cập nhật item này
                        Log.d("ImageResolverTask", "Updated image for item at index " + index);
                    }

                    // Log tiến trình
                    int completed = completedCount.incrementAndGet();
                    Log.d("ImageResolverTask", "Progress: " + completed + "/" + totalItems + " images loaded");
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dọn dẹp executor service khi activity bị hủy
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void fetchMostViewedPlaces() {
        if (isLoading) return; // Nếu đang tải thì không gọi nữa
        Log.d("FetchData", "Đang tải Most Viewed...");
        isLoading = true;

        // Xóa danh sách cũ
        placeList.clear();
        adapter.updateData(placeList);

        apiService.getMostViewedPlaces().enqueue(new Callback<List<PlaceItem>>() {
            @Override
            public void onResponse(Call<List<PlaceItem>> call, Response<List<PlaceItem>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("FetchData", "Tải Most Viewed thành công: " + response.body().size() + " mục.");
                    adapter.updateData(response.body()); // Cập nhật adapter với dữ liệu mới
                } else {
                    Log.e("FetchData", "Lỗi API Most Viewed: " + response.code());
                    Toast.makeText(HomeScreen.this, "Lỗi tải Most Viewed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PlaceItem>> call, Throwable t) {
                isLoading = false;
                Log.e("FetchData", "Lỗi mạng Most Viewed", t);
                Toast.makeText(HomeScreen.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLatestPlaces() {
        if (isLoading) return; // Nếu đang tải thì không gọi nữa
        Log.d("FetchData", "Đang tải Latest...");
        isLoading = true;

        // Xóa danh sách cũ
        placeList.clear();
        adapter.updateData(placeList);

        apiService.getLatestPlaces().enqueue(new Callback<List<PlaceItem>>() {
            @Override
            public void onResponse(Call<List<PlaceItem>> call, Response<List<PlaceItem>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("FetchData", "Tải Latest thành công: " + response.body().size() + " mục.");
                    adapter.updateData(response.body()); // Cập nhật adapter
                } else {
                    Log.e("FetchData", "Lỗi API Latest: " + response.code());
                    Toast.makeText(HomeScreen.this, "Lỗi tải Latest", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PlaceItem>> call, Throwable t) {
                isLoading = false;
                Log.e("FetchData", "Lỗi mạng Latest", t);
                Toast.makeText(HomeScreen.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}