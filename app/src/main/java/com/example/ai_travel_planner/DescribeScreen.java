package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DescribeScreen extends Activity {
    ImageButton btnBack, btnBookmark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.describescreen);

        String placeName = getIntent().getStringExtra("placeName");
        TextView tvTitle = findViewById(R.id.tvPlaceName);
        tvTitle.setText(placeName);

        btnBack = findViewById(R.id.btnBack);
        btnBookmark = findViewById(R.id.btnBookmark);

        actionButton(btnBack, HomeScreen.class);
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

