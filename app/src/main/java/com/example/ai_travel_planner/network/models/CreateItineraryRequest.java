package com.example.ai_travel_planner.network.models;

// Class này phải khớp với các trường Backend cần
public class CreateItineraryRequest {
    String title;
    Integer days;
    String style;
    String payload; // Sẽ chứa JSON của TripPlan

    // Constructor để tạo từ TripPlan
    public CreateItineraryRequest(TripPlan plan, String planJson) {
        this.title = plan.destination; // Giả sử title là destination
        this.days = plan.getDaysCount();
        this.style = plan.style;
        this.payload = planJson;
    }
}