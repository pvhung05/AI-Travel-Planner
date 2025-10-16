package com.example.ai_travel_planner.ui.home.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.models.LoginRequest;
import com.example.ai_travel_planner.network.models.LoginResponse;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginScreen extends Activity {
    ImageButton btnBack, btnGoogle, btnFacebook, btnApple;
    Button btnSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);

        btnBack = findViewById(R.id.btnBack);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnApple = findViewById(R.id.btnApple);
        btnSignin = findViewById(R.id.btnSignIn);

        // Back về Start
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, StartScreen.class));
            finish();
        });

        // Sign In -> Gọi API
        btnSignin.setOnClickListener(v -> {
            // TODO: Nếu có EditText, đọc từ đó. Tạm hard-code để test.
            String email = "test@example.com";
            String password = "123456";

            ApiService api = ApiClient.getClient().create(ApiService.class);
            api.login(new LoginRequest(email, password))
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String token = response.body().getToken();
                                Toast.makeText(LoginScreen.this, "✅ Login success\nJWT: " + token, Toast.LENGTH_LONG).show();

                                // chuyển màn hình
                                startActivity(new Intent(LoginScreen.this, HomeScreen.class));
                                finish();
                            } else {
                                Toast.makeText(LoginScreen.this, "❌ Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(LoginScreen.this, "⚠️ Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
