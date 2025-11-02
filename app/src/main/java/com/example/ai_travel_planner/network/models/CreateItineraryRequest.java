package com.example.ai_travel_planner.network.models;

public class CreateItineraryRequest {
    String title;
    Integer days;
    String style;
    String payload;

    // Constructor để tạo từ TripPlan
    public CreateItineraryRequest(TripPlan plan, String planJson) {
        this.title = plan.destination; // Giả sử title là destination
        this.days = plan.getDaysCount();
        this.style = plan.style;
        this.payload = planJson;
    }
}