package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DescribeScreen extends Activity {
    ImageButton btnBack, btnBookmark;
    Button btnEnterPlan, btnViewOther;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.describescreen);

        String placeName = getIntent().getStringExtra("placeName");
        TextView tvTitle = findViewById(R.id.tvPlaceName);
        tvTitle.setText(placeName);

        btnBack = findViewById(R.id.btnBack);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnEnterPlan = findViewById(R.id.btnEnterPlan);
        btnViewOther = findViewById(R.id.btnViewOther);

        actionButton(btnBack, HomeScreen.class);
        actionButton(btnViewOther, HomeScreen.class);
        actionButton(btnEnterPlan, PlanScreen.class);
    }

    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DescribeScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }
}

