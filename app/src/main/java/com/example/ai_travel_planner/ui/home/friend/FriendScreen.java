package com.example.ai_travel_planner.ui.home.friend;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // THÊM IMPORT NÀY
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;
import com.example.ai_travel_planner.ui.home.profile.ProfileScreen;

import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.models.InviteLinkResponse;
import com.example.ai_travel_planner.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendScreen extends AppCompatActivity {

    Button btnEditProfile, btnInviteFriends;
    ImageView btnLink, btnHome;

    private long itineraryId;
    private ApiService apiService;
    private String authToken; // THÊM BIẾN ĐỂ GIỮ TOKEN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendscreen);

        // --- BƯỚC 1: LẤY ID CHUYẾN ĐI ---
        itineraryId = getIntent().getLongExtra("ITINERARY_ID", -1);

        // 1. Dùng TokenManager (giống hệt LoginScreen)
        TokenManager tokenManager = new TokenManager(this);

// 2. Lấy token ra (Tôi đoán tên hàm là .getToken(),
//    bạn hãy mở file TokenManager.java để xem tên hàm đúng)
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Lỗi: Bạn chưa đăng nhập!", Toast.LENGTH_LONG).show();
            // Ẩn các nút mời nếu chưa đăng nhập
            findViewById(R.id.btnInviteFriends).setVisibility(View.GONE);
            findViewById(R.id.btnLink).setVisibility(View.GONE);
        } else {
            // Gắn "Bearer " vào trước token
            if (!token.startsWith("Bearer ")) {
                this.authToken = "Bearer " + token;
            } else {
                this.authToken = token;
            }
        }
        // --- KẾT THÚC LẤY TOKEN ---

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnInviteFriends = findViewById(R.id.btnInviteFriends);
        btnLink = findViewById(R.id.btnLink);
        btnHome = findViewById(R.id.btnHome);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(FriendScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FriendScreen.this, ProfileScreen.class);
            startActivity(intent);
        });

        // Nút này đã đúng (truyền ID sang InviteFriendScreen)
        btnInviteFriends.setOnClickListener(v -> {
            if (itineraryId == -1) {
                Toast.makeText(this, "Lỗi: Không tìm thấy chuyến đi!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(FriendScreen.this, InviteFriendScreen.class);
            intent.putExtra("ITINERARY_ID", itineraryId);
            startActivity(intent);
        });


        btnLink.setOnClickListener(v -> {
            if (itineraryId == -1) {
                Toast.makeText(this, "Lỗi: Không tìm thấy chuyến đi!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (authToken == null || authToken.isEmpty()) {
                Toast.makeText(this, "Lỗi: Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            showLinkPopup(v, itineraryId);
        });
    }

    private void showLinkPopup(View anchorView, long tripId) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.link_popup, null);

        EditText editLink = popupView.findViewById(R.id.editLink);
        ImageView btnCopy = popupView.findViewById(R.id.btnCopy);

        editLink.setText("Đang tạo link...");
        editLink.setEnabled(false);
        btnCopy.setEnabled(false);

        // --- BƯỚC 3: SỬA LẠI API CALL (TRUYỀN TOKEN VÀ ID) ---
        Call<InviteLinkResponse> call = apiService.getInviteLink(authToken, tripId);

        call.enqueue(new Callback<InviteLinkResponse>() {
            @Override
            public void onResponse(Call<InviteLinkResponse> call, Response<InviteLinkResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    editLink.setText(response.body().getLink());
                    btnCopy.setEnabled(true);
                } else {
                    editLink.setText("Lỗi khi tạo link.");
                }
            }

            @Override
            public void onFailure(Call<InviteLinkResponse> call, Throwable t) {
                editLink.setText("Lỗi mạng: Không thể tạo link.");
            }
        });
        // --- KẾT THÚC SỬA ---

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.showAtLocation(anchorView, Gravity.BOTTOM, 0, 0);

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Link", editLink.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép!", Toast.LENGTH_SHORT).show();
        });
    }
}