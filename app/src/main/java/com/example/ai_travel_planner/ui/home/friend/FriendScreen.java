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
import com.example.ai_travel_planner.utils.BottomNav;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FriendScreen extends AppCompatActivity {

    Button btnEditProfile, btnInviteFriends;
    ImageView btnLink, btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendscreen);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnInviteFriends = findViewById(R.id.btnInviteFriends);
        btnLink = findViewById(R.id.btnLink);
        btnHome = findViewById(R.id.btnHome);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(FriendScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

        // Mở trang Profile
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FriendScreen.this, ProfileScreen.class);
            startActivity(intent);
        });

        // Chuyển sang màn hình mời bạn bè
        btnInviteFriends.setOnClickListener(v -> {
            Intent intent = new Intent(FriendScreen.this, InviteFriendScreen.class);
            startActivity(intent);
        });

        // Hiển thị popup link
        btnLink.setOnClickListener(v -> showLinkPopup(v));
    }

    private void showLinkPopup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.link_popup, null);

        EditText editLink = popupView.findViewById(R.id.editLink);
        ImageView btnCopy = popupView.findViewById(R.id.btnCopy);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Hiển thị ngay phía trên thanh nút Invite
        popupWindow.showAtLocation(anchorView, Gravity.BOTTOM, 0, 0);

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Link", editLink.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
        });
    }
}
