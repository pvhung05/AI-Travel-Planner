    package com.example.ai_travel_planner.ui.home.login;

    import android.app.Activity;
    import android.content.Intent;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.Toast;

    import com.example.ai_travel_planner.R;
    import com.example.ai_travel_planner.network.ApiClient;
    import com.example.ai_travel_planner.network.ApiService;
    import com.example.ai_travel_planner.network.models.RegisterRequest;
    import com.example.ai_travel_planner.network.models.RegisterResponse;
    import com.example.ai_travel_planner.ui.home.home.HomeScreen;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;
    import retrofit2.Retrofit;

    public class SigninScreen extends Activity {
        ImageButton btnBack, btnGoogle, btnFacebook, btnApple;
        Button btnRegister;

        EditText etEmail, etPassword, etConfirmPassword, etDisplayName;

        ApiService apiService;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.signinscreen);

            //Khởi tạo API servive
            apiService = ApiClient.getClient().create(ApiService.class);

            //Khởi tạo các view
            etDisplayName = findViewById(R.id.etDisplayName);
            btnBack = findViewById(R.id.btnBack);
            btnGoogle = findViewById(R.id.btnGoogle);
            btnFacebook = findViewById(R.id.btnFacebook);
            btnApple = findViewById(R.id.btnApple);

            //Ánh xạ các view mới
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            btnRegister = findViewById(R.id.btnSignUp);

            //Gán sự kiện cho nút back
            actionButton(btnBack, StartScreen.class);

            // Gán sự kiện cho nút Register
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleRegistration();
                }
            });
        }

        private void handleRegistration() {
            String displayName = etDisplayName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String comfirmPassword = etConfirmPassword.getText().toString().trim();

            //Kiểm tra đầu vào
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(comfirmPassword) || TextUtils.isEmpty(displayName)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(comfirmPassword)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            //Tạo Request Body
            RegisterRequest registerRequest = new RegisterRequest(email, password, displayName);

            //Gọi API
            apiService.register(registerRequest).enqueue(new Callback<RegisterResponse>() {
                @Override
                public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        //Đăng ký thành công
                        Toast.makeText(SigninScreen.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        //Lưu token
                        String token = response.body().getToken();
                        Log.d("Đăng ký thành công", "Token:" + token);
                        //Chuyển đến màn hình chính
                        Intent intent = new Intent(SigninScreen.this, HomeScreen.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SigninScreen.this, "Đăng ký thất bại. Email đã được sử dụng.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<RegisterResponse> call, Throwable t) {
                    Log.e("Đăng ký thất bại", t.getMessage(), t);
                    Toast.makeText(SigninScreen.this, "Lỗi mạng:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        void actionButton(View view, final Class<?> destination) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SigninScreen.this, destination);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
