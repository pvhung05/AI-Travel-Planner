package com.example.ai_travel_planner.network.models;

// Class này để hứng response từ POST /api/itineraries
// Nó phải khớp với file Itinerary.java của Backend
public class ItineraryResponse {
    private Long id;
    private String title;

    // Thêm các trường khác nếu bạn cần

    public Long getId() {
        return id;
    }
}