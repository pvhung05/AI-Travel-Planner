package com.example.ai_travel_planner.network.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Một ngày trong TripPlan, chứa danh sách ActivityItem.
 */
public class DayPlan implements Serializable {
    // Nhãn hiển thị: "Day 1", "Day 2" hoặc "Ngày 1" tuỳ UI
    public String label;

    // Ngày thực tế (tuỳ chọn): "2025-07-14" hoặc "July 14"
    public String date;

    // Danh sách hoạt động trong ngày
    public List<ActivityItem> activities = new ArrayList<>();
}
