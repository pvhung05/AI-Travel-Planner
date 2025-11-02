package com.example.ai_travel_planner.network.models;

import java.io.Serializable;
import java.util.UUID;

public class ActivityItem implements Serializable {
    public String id = UUID.randomUUID().toString();
    public String time;      // "05:30"
    public String title;     // "Wake up"
    public String category;  // "food|nature|culture|shopping|adventure|other"
    public String location;  // optional
    public Integer costVND;  // optional
    public String notes;     // optional
    public String emoji;     // optional
}
