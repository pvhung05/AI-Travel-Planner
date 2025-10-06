package com.example.ai_travel_planner.ui.home.favorite;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.utils.BottomNav;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteScreen extends AppCompatActivity {

    private RecyclerView recyclerFavorites;
    private FavoriteAdapter favoriteAdapter;
    private List<FavoriteItem> favoriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favotitesscreen); // layout chứa RecyclerView

        recyclerFavorites = findViewById(R.id.rvFavorites);
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu mẫu
        favoriteList = new ArrayList<>();
        favoriteList.add(new FavoriteItem("Đà Nẵng", "Việt Nam", (float) 4.8, R.drawable.place_image));
        favoriteList.add(new FavoriteItem("Hội An", "Quảng Nam", (float) 4.9, R.drawable.place_image));
        favoriteList.add(new FavoriteItem("Đà Nẵng", "Việt Nam", (float) 4.8, R.drawable.place_image));
        favoriteList.add(new FavoriteItem("Hội An", "Quảng Nam", (float) 4.9, R.drawable.place_image));
        favoriteList.add(new FavoriteItem("Đà Nẵng", "Việt Nam", (float) 4.8, R.drawable.place_image));
        favoriteList.add(new FavoriteItem("Hội An", "Quảng Nam", (float) 4.9, R.drawable.place_image));
        favoriteList.add(new FavoriteItem("Đà Nẵng", "Việt Nam", (float) 4.8, R.drawable.place_image));
        favoriteList.add(new FavoriteItem("Hội An", "Quảng Nam", (float) 4.9, R.drawable.place_image));

        favoriteAdapter = new FavoriteAdapter(favoriteList);
        recyclerFavorites.setAdapter(favoriteAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_favorites);
    }
}
