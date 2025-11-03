package com.example.ai_travel_planner.ui.home.friend;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
    ImageView btnLink, btnBack;

    private long itineraryId;
    private ApiService apiService;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendscreen);

        itineraryId = getIntent().getLongExtra("ITINERARY_ID", -1);

        TokenManager tokenManager = new TokenManager(this);

        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Lỗi: Bạn chưa đăng nhập!", Toast.LENGTH_LONG).show();
            findViewById(R.id.btnInviteFriends).setVisibility(View.GONE);
            findViewById(R.id.btnLink).setVisibility(View.GONE);
        } else {
            if (!token.startsWith("Bearer ")) {
                this.authToken = "Bearer " + token;
            } else {
                this.authToken = token;
            }
        }

        apiService = ApiClient.getClient().create(ApiService.class);


        btnInviteFriends = findViewById(R.id.btnInviteFriends);
        btnLink = findViewById(R.id.btnLink);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());


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