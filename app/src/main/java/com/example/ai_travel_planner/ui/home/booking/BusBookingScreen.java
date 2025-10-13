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

public class BusBookingScreen extends Activity {
    ImageButton btnBack;
    LinearLayout tabPlane;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_booking);
        btnBack = findViewById(R.id.btnBack);
        tabPlane = findViewById(R.id.tabPlane);
        btnSearch = findViewById(R.id.btnSearch);

        actionButton(btnBack, GuideScreen.class);
        actionButton(tabPlane, PlaneBookingScreen.class);
    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusBookingScreen.this, destination);
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