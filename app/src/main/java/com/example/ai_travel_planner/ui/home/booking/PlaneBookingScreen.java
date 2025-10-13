package com.example.ai_travel_planner.ui.home.booking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;
import com.example.ai_travel_planner.ui.home.login.SigninScreen;
import com.example.ai_travel_planner.ui.home.plan.GuideScreen;
import com.example.ai_travel_planner.ui.home.plan.PlanScreen;

public class PlaneBookingScreen extends Activity {
    ImageButton btnBack;
    LinearLayout tabBus;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plane_booking);
        btnBack = findViewById(R.id.btnBack);
        tabBus = findViewById(R.id.tabBus);
        btnSearch = findViewById(R.id.btnSearch);

        actionButton(btnBack, GuideScreen.class);
        actionButton(tabBus, BusBookingScreen.class);
    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaneBookingScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish(); // Quay lại màn hình trước đó
    }
}