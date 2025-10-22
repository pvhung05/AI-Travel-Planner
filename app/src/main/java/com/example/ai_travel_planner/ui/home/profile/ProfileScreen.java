package com.example.ai_travel_planner.ui.home.profile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.network.ApiClient;
import com.example.ai_travel_planner.network.ApiService;
import com.example.ai_travel_planner.network.models.UserMe;
import com.example.ai_travel_planner.ui.home.friend.FriendScreen;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.login.StartScreen;
import com.example.ai_travel_planner.utils.BottomNav;
import com.example.ai_travel_planner.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileScreen extends AppCompatActivity {
    ImageButton btnBack;
    Button btnSave;
    LinearLayout btnLogout;

    ImageView ivProfilePicture;
    EditText edtName, edtEmail, edtPassword, edtPasswordConfirm;
    ApiService apiService;

    //Mã nhận diện hành động chọn ảnh
    private static final int PICK_IMAGE_REQUEST = 1;
    //Biến lưu đường dẫn ảnh được chọn
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        //Khởi tạo API servive
        apiService = ApiClient.getClient().create(ApiService.class);

        //Khởi tạo các view
        ivProfilePicture = findViewById(R.id.imgAvatar);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPasswordConfirm = findViewById(R.id.edtPasswordConfirm);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);


        //Gán sự kiện cho nút Back
        btnBack.setOnClickListener(v -> finish());

        //Gán sự kiện cho nút Logout
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, StartScreen.class));
            finish();
        });

        //Gán sự kiện cho nút Save
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUpdateProfile();
            }
        });

        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_profile);
    }

    //Mo thu vien anh
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Hàm nhận kết quả sau khi chọn ảnh
     * Chỉ lưu Uri lại, KHÔNG upload vội
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfilePicture.setImageURI(imageUri);
        }
    }
    private void handleUpdateProfile() {
        String displayName = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtPasswordConfirm.getText().toString().trim();

        RequestBody displayNameBody = null;
        if (!displayName.isEmpty()) {
            displayNameBody = createPartFromString(displayName);
        }

        RequestBody emailBody = null;
        if (!email.isEmpty()) {
            emailBody = createPartFromString(email);
        }

        RequestBody passwordBody = null;
        if (!password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            passwordBody = createPartFromString(password);
        }

        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            imagePart = prepareFilePart("avatarUrl", imageUri);
            if (imagePart == null) {
                Toast.makeText(this, "Lỗi khi đọc file ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(this, "Đang cập nhật...", Toast.LENGTH_SHORT).show();

        TokenManager tokenManager = new TokenManager(this);
        String authToken = tokenManager.getToken();

        apiService.updateProfile("Bearer " + authToken, displayNameBody, emailBody, passwordBody, imagePart)
                .enqueue(new Callback<UserMe>() {
                    @Override
                    public void onResponse(Call<UserMe> call, Response<UserMe> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ProfileScreen.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            imageUri = null;
                        } else {
                            Toast.makeText(ProfileScreen.this, "Cập nhật thất bại.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserMe> call, Throwable t) {
                        Log.e("PROFILE_UPDATE_FAIL", "Lỗi mạng", t);
                        Toast.makeText(ProfileScreen.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private RequestBody createPartFromString(String descriptionString) {
        if (descriptionString == null) {
            descriptionString = "";
        }
        return RequestBody.create(MediaType.parse("text/plain"), descriptionString);
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            String fileName = getFileName(contentResolver, fileUri);
            String mimeType = contentResolver.getType(fileUri);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            File tempFile = File.createTempFile("upload", getFileExtension(fileName), getCacheDir());
            InputStream inputStream = contentResolver.openInputStream(fileUri);
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);
            return MultipartBody.Part.createFormData(partName, fileName, requestFile);
        } catch (Exception e) {
            Log.e("FilePrep", "Lỗi khi chuẩn bị file", e);
            return null;
        }
    }
    private String getFileName(ContentResolver contentResolver, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "unknown_file";
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) return fileName.substring(dotIndex);
        return ".tmp";
    }
}
