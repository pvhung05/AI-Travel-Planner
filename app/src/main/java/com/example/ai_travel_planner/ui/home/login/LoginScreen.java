package com.example.ai_travel_planner.ui.home.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.models.LoginRequest;
import com.example.ai_travel_planner.network.models.LoginResponse;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;
import com.example.ai_travel_planner.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginScreen extends Activity {
    ImageButton btnBack, btnGoogle, btnFacebook, btnApple;
    Button btnSignIn;

    EditText etEmail, etPassword;  // <-- dùng đúng ID trong XML

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);

        btnBack = findViewById(R.id.btnBack);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnApple = findViewById(R.id.btnApple);
        btnSignIn = findViewById(R.id.btnSignIn);

        etEmail = findViewById(R.id.etEmail);       // <-- TRÙNG ID
        etPassword = findViewById(R.id.etPassword); // <-- TRÙNG ID

        TokenManager tokenManager = new TokenManager(this);
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // Back
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, StartScreen.class));
            finish();
        });

        // Đăng nhập
        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            api.login(new LoginRequest(email, password))
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                                String token = response.body().getToken();
                                tokenManager.saveToken(token);
                                Toast.makeText(LoginScreen.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(LoginScreen.this, HomeScreen.class));
                                finish();
                            } else {
                                Toast.makeText(LoginScreen.this, "Đăng nhập thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(LoginScreen.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}