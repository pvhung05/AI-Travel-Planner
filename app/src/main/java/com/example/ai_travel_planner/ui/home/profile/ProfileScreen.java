package com.example.ai_travel_planner.ui.home.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.ai_travel_planner.ui.home.friend.FriendScreen;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.login.StartScreen;
import com.example.ai_travel_planner.utils.BottomNav;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileScreen extends Activity {
    ImageButton btnBack;
    Button btnSave;
    LinearLayout btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);

        actionButton(btnBack, FriendScreen.class);
        actionButton(btnLogout, StartScreen.class);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_profile);


    }
    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }
}
