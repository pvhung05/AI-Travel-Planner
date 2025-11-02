package com.example.ai_travel_planner.ui.home.friend;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import com.example.ai_travel_planner.R;

public class InviteFriendScreen extends AppCompatActivity {
    EditText emailInput;

    Button btnInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_friend_screen);

        emailInput = findViewById(R.id.emailInput);
        btnInvite = findViewById(R.id.btnInvite);

        btnInvite.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            // Gửi email mời bạn bè mặc định mời vào luôn xong là quay trở lại màn hình FriendScreen và thêm bạn vào danh sách bạn bè
        });
    }
}
