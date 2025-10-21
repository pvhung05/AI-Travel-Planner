package com.example.ai_travel_planner.ui.home.home;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.models.UserMe;
import com.example.ai_travel_planner.utils.BottomNav;
import com.example.ai_travel_planner.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreen extends AppCompatActivity {

    RecyclerView recyclerView;
    PlaceAdapter adapter;
    List<String> places;
    TextView tvGreeting; // <- để set "Hi, <name>"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        // 1) Ánh xạ view
        tvGreeting = findViewById(R.id.tvGreeting);
        recyclerView = findViewById(R.id.recyclerPlaces);

        // 2) Recycler ngang (demo dữ liệu tĩnh)
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        places = Arrays.asList("Hà Nội", "Đà Nẵng", "TP. Hồ Chí Minh", "Nha Trang", "Phú Quốc");
        adapter = new PlaceAdapter(places);
        recyclerView.setAdapter(adapter);

        // 3) Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_home);

        // 4) Gọi API /api/user/me để lấy tên thật từ token
        fetchCurrentUserName();
    }

    private void fetchCurrentUserName() {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            // chưa đăng nhập hoặc chưa có token
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getMe("Bearer " + token).enqueue(new Callback<UserMe>() {
            @Override
            public void onResponse(Call<UserMe> call, Response<UserMe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String name = response.body().getDisplayName();
                    if (name == null || name.trim().isEmpty()) {
                        // fallback dùng email nếu chưa có displayName
                        name = response.body().getEmail();
                    }
                    if (name == null || name.trim().isEmpty()) {
                        name = "Traveler";
                    }
                    tvGreeting.setText("Hi, " + name + " 👋");
                } else {
                    Toast.makeText(HomeScreen.this,
                            "Không lấy được thông tin user (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserMe> call, Throwable t) {
                Toast.makeText(HomeScreen.this, "Lỗi mạng: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}