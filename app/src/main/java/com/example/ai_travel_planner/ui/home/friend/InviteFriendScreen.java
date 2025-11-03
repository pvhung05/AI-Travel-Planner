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

import com.example.ai_travel_planner.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteFriendScreen extends AppCompatActivity {
    EditText emailInput;
    ImageView btnHome, btnBack;
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
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        apiService = ApiClient.getClient().create(ApiService.class);

        itineraryId = getIntent().getLongExtra("ITINERARY_ID", -1);

        TokenManager tokenManager = new TokenManager(this);

        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Lỗi: Bạn chưa đăng nhập!", Toast.LENGTH_LONG).show();
            btnInvite.setEnabled(false);
        } else {
            if (!token.startsWith("Bearer ")) {
                this.authToken = "Bearer " + token;
            } else {
                this.authToken = token;
            }
        }

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