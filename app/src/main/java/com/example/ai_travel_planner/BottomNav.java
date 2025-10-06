package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNav {

    public static void setupNavigation(Activity activity, BottomNavigationView bottomNav, int selectedItemId) {
        // Đánh dấu mục đang chọn
        bottomNav.setSelectedItemId(selectedItemId);

        // Lắng nghe khi người dùng chọn mục khác
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Nếu đang ở chính màn hình đó thì không làm gì
            if (id == selectedItemId) return true;

            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(activity, HomeScreen.class);
            } else if (id == R.id.nav_time) {
                intent = new Intent(activity, PlanScreen.class);
            } else if (id == R.id.nav_favorites) {
                intent = new Intent(activity, FriendScreen.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(activity, ProfileScreen.class);
            }

            // Chuyển sang activity tương ứng (nếu có)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }

            return true;
        });
    }
}
