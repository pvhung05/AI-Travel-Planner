package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.ai_travel_planner.ui.home.login.StartScreen;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // You might want to add some delay or loading screen here
        Intent intent = new Intent(MainActivity.this, StartScreen.class);
        startActivity(intent);
        finish();
    }
}