package com.example.ai_travel_planner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;


public class CalendarScreen extends AppCompatActivity {

    ImageButton btnBack, btnSendQuery;
    Switch switchEmail;
    Button btnNextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendarscreen);

        btnBack = findViewById(R.id.btnBack);
        switchEmail = findViewById(R.id.switchEmail);
        btnNextStep = findViewById(R.id.btnNextStep);

        actionButton(btnBack, GuideScreen.class);
        actionButton(btnNextStep, AddItineraryScreen.class);

    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }

}
