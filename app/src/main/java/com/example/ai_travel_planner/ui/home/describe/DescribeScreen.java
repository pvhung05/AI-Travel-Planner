package com.example.ai_travel_planner.ui.home.describe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;
import com.example.ai_travel_planner.ui.home.plan.PlanScreen;

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

        // Nút back chỉ cần finish() để quay lại màn hình trước
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEnterPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DescribeScreen.this, PlanScreen.class);
                startActivity(intent);
                finish();
            }
        });

        btnViewOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DescribeScreen.this, HomeScreen.class);
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