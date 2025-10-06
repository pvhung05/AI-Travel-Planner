package com.example.ai_travel_planner;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    RecyclerView recyclerView;
    PlaceAdapter adapter;
    List<String> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        recyclerView = findViewById(R.id.recyclerPlaces);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        places = Arrays.asList("Hà Nội", "Đà Nẵng", "TP. Hồ Chí Minh", "Nha Trang", "Phú Quốc");

        adapter = new PlaceAdapter(places);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_home);

    }

}
