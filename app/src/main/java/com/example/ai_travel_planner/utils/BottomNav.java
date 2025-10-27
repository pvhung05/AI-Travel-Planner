package com.example.ai_travel_planner.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.friend.FriendScreen;
import com.example.ai_travel_planner.ui.home.home.HomeScreen;
import com.example.ai_travel_planner.ui.home.plan.PlanScreen;
import com.example.ai_travel_planner.ui.home.profile.ProfileScreen;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNav {

    public static void setupNavigation(Activity activity, BottomNavigationView bottomNav, int selectedItemId) {
        // Áp dụng màu cho icon & text
        bottomNav.setItemIconTintList(activity.getColorStateList(R.color.nav_item_color));
        bottomNav.setItemTextColor(activity.getColorStateList(R.color.nav_item_color));

        // Đánh dấu tab đang được chọn
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Nếu chọn lại đúng màn hình hiện tại thì không làm gì
            if (id == selectedItemId) return true;

            Intent intent = null;

            if (id == R.id.nav_home) {
                if (!(activity instanceof HomeScreen)) {
                    intent = new Intent(activity, HomeScreen.class);
                }
            }
            else if (id == R.id.nav_time) {
                if (!(activity instanceof PlanScreen)) {
                    intent = new Intent(activity, PlanScreen.class);
                }
            }
            else if (id == R.id.nav_group) {
                if (!(activity instanceof FriendScreen)) {
                    intent = new Intent(activity, FriendScreen.class);
                }
            }
            else if (id == R.id.nav_profile) {
                if (!(activity instanceof ProfileScreen)) {
                    intent = new Intent(activity, ProfileScreen.class);
                }
            }

            if (intent != null) {
                // Không reload activity cũ, chỉ chuyển sang activity mới
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish(); // Đóng activity cũ để tránh bị loạn màu
            }

            return true;
        });
    }
}
