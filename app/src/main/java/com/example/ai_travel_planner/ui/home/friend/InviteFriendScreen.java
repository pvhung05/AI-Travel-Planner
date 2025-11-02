package com.example.ai_travel_planner.ui.home.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;

import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.models.ItineraryCollaborator;

// THÊM IMPORT NÀY (GIỐNG BÊN LOGINSCREEN)
import com.example.ai_travel_planner.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteFriendScreen extends AppCompatActivity {
    EditText emailInput;
    ImageView btnHome;
    Button btnInvite;

    private ApiService apiService;
    private long itineraryId;
    private String authToken;

    private static final String DEFAULT_ROLE = "EDITOR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_friend_screen);

        emailInput = findViewById(R.id.emailInput);
        btnInvite = findViewById(R.id.btnInvite);
        btnHome = findViewById(R.id.btnHome);

        // --- BƯỚC 1: KHỞI TẠO API SERVICE ---
        apiService = ApiClient.getClient().create(ApiService.class);

        // --- BƯỚC 2: NHẬN ID CHUYẾN ĐI ---
        itineraryId = getIntent().getLongExtra("ITINERARY_ID", -1);

        // --- BƯỚC 3: SỬA LẠI CÁCH LẤY AUTH TOKEN ---
        // (Xóa code SharedPreferences cũ)

        // 1. Dùng TokenManager (giống hệt LoginScreen)
        TokenManager tokenManager = new TokenManager(this);

        // 2. Lấy token ra (Tôi đoán tên hàm là .getToken(),
        //    bạn hãy mở file TokenManager.java để xem tên hàm đúng)
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Lỗi: Bạn chưa đăng nhập!", Toast.LENGTH_LONG).show();
            btnInvite.setEnabled(false); // Vô hiệu hóa nút nếu không có token
        } else {
            // Gắn "Bearer " vào trước token
            if (!token.startsWith("Bearer ")) {
                this.authToken = "Bearer " + token;
            } else {
                this.authToken = token;
            }
        }
        // --- KẾT THÚC SỬA ---

        if (itineraryId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy chuyến đi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(InviteFriendScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

        // --- BƯỚC 4: TRIỂN KHAI NÚT MỜI (Code này đã đúng) ---
        btnInvite.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (authToken == null || authToken.isEmpty()) {
                Toast.makeText(this, "Lỗi: Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isValidEmail(email)) {
                sendInvitation(authToken, email, itineraryId);
            } else {
                emailInput.setError("Email không hợp lệ");
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Hàm này đã đúng (vì nó đã nhận 'token' làm tham số)
    private void sendInvitation(String token, String email, long tripId) {
        btnInvite.setEnabled(false);
        btnInvite.setText("Đang mời...");

        Call<ItineraryCollaborator> call = apiService.inviteCollaborator(token, tripId, email, DEFAULT_ROLE);

        call.enqueue(new Callback<ItineraryCollaborator>() {
            @Override
            public void onResponse(Call<ItineraryCollaborator> call, Response<ItineraryCollaborator> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InviteFriendScreen.this, "Đã thêm bạn thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(InviteFriendScreen.this, "Lỗi: Phiên đăng nhập hết hạn.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(InviteFriendScreen.this, "Lỗi: Email không tồn tại hoặc đã ở trong nhóm.", Toast.LENGTH_LONG).show();
                    }
                    btnInvite.setEnabled(true);
                    btnInvite.setText("Mời");
                }
            }

            @Override
            public void onFailure(Call<ItineraryCollaborator> call, Throwable t) {
                Toast.makeText(InviteFriendScreen.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                btnInvite.setEnabled(true);
                btnInvite.setText("Mời");
            }
        });
    }
}