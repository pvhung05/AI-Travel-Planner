package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class WeatherScreen extends Activity {

    ImageButton btnBack;
    Button btnViewItinerary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weatherscreen);

        btnBack = findViewById(R.id.btnBack);
        btnViewItinerary = findViewById(R.id.btnViewItinerary);

        actionButton(btnBack, PlanScreen.class);
        actionButton(btnViewItinerary, GuideScreen.class);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_time);
    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }
}

