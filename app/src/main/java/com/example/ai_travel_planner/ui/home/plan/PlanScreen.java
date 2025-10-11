package com.example.ai_travel_planner.ui.home.plan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.utils.BottomNav;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlanScreen extends AppCompatActivity {

    Button tvLocationTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planscreen);

        tvLocationTag = findViewById(R.id.tvLocationTag);

        actionButton(tvLocationTag, WeatherScreen.class);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_time);


    }
    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlanScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }

}
