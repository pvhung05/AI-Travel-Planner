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
    Button btnAddActivity, btnNextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_itinerary);

        btnAddActivity = findViewById(R.id.btnAddActivity);

        actionButton(btnNextStep, FriendScreen.class);

        btnBack.setOnClickListener(v -> finish());
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
