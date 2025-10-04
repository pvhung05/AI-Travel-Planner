package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ProfileScreen extends Activity {
    ImageButton btnBack;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        actionButton(btnBack, FriendScreen.class);

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
