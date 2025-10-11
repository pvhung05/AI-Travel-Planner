package com.example.ai_travel_planner.ui.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.ui.home.plan.CalendarScreen;
import com.example.ai_travel_planner.ui.home.friend.FriendScreen;
import com.example.ai_travel_planner.R;

public class AddItineraryScreen extends AppCompatActivity {

    ImageButton btnBack;
    ImageView ivNext1, ivNext2;
    Button btnAddActivity, btnNextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_itinerary);

        btnBack = findViewById(R.id.btnBack);
        btnNextStep = findViewById(R.id.btnNextStep);
        btnAddActivity = findViewById(R.id.btnAddActivity);

        actionButton(btnBack, CalendarScreen.class);
        actionButton(btnNextStep, FriendScreen.class);


    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddItineraryScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }

}
