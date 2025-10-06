package com.example.ai_travel_planner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class FriendScreen extends AppCompatActivity {

    ImageButton btnHome;
    Button btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendscreen);

        btnHome = findViewById(R.id.btnHome);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        actionButton(btnHome, HomeScreen.class);
        actionButton(btnEditProfile, ProfileScreen.class);
    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }

}
