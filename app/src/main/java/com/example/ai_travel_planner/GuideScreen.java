package com.example.ai_travel_planner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class GuideScreen extends AppCompatActivity {

    ImageButton btnBack;
    ImageView ivNext1, ivNext2;
    Button btnPlanTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guidescreen);

        btnBack = findViewById(R.id.btnBack);
        ivNext1 = findViewById(R.id.ivNext1);
        ivNext2 = findViewById(R.id.ivNext2);
        btnPlanTrip = findViewById(R.id.btnPlanTrip);

        actionButton(btnBack, WeatherScreen.class);
        actionButton(btnPlanTrip, CalendarScreen.class);

    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuideScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }

}
