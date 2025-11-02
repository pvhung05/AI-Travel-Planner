package com.example.ai_travel_planner.ui.home.friend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;

public class InviteFriendScreen extends AppCompatActivity {
    EditText emailInput;

    ImageView btnHome;

    Button btnInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_friend_screen);

        emailInput = findViewById(R.id.emailInput);
        btnInvite = findViewById(R.id.btnInvite);
        btnHome = findViewById(R.id.btnHome);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(InviteFriendScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

        btnInvite.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            // Gửi email mời bạn bè mặc định mời vào luôn xong là quay trở lại màn hình FriendScreen và thêm bạn vào danh sách bạn bè
        });
    }
}
