package com.example.ai_travel_planner.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.favorite.FavoriteScreen;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;
import com.example.ai_travel_planner.ui.home.plan.PlanScreen;
import com.example.ai_travel_planner.ui.home.profile.ProfileScreen;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNav {

    public static void setupNavigation(Activity activity, BottomNavigationView bottomNav, int selectedItemId) {
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Nếu đang ở chính màn hình đó thì không làm gì
            if (id == selectedItemId) return true;

            Intent intent = null;

            if (id == R.id.nav_home) {
                // Kiểm tra nếu không phải đang ở HomeScreen thì mới chuyển
                if (!(activity instanceof HomeScreen)) {
                    intent = new Intent(activity, HomeScreen.class);
                }
            } else if (id == R.id.nav_time) {
                if (!(activity instanceof PlanScreen)) {
                    intent = new Intent(activity, PlanScreen.class);
                }
            } else if (id == R.id.nav_favorites) {
                if (!(activity instanceof FavoriteScreen)) {
                    intent = new Intent(activity, FavoriteScreen.class);
                }
            } else if (id == R.id.nav_profile) {
                if (!(activity instanceof ProfileScreen)) {
                    intent = new Intent(activity, ProfileScreen.class);
                }
            }

            if (intent != null) {
                // Không dùng CLEAR_TOP để tránh reload
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);

                // Đóng activity hiện tại nếu cần
                // activity.finish();
            }

            return true;
        });
    }
}