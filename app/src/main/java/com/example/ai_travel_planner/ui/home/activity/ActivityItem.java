package com.example.ai_travel_planner.ui.home.activity;

public class ActivityItem {
    private String time;
    private String title;
    private int iconRes;

    public ActivityItem(String time, String title, int iconRes) {
        this.time = time;
        this.title = title;
        this.iconRes = iconRes;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }
}
