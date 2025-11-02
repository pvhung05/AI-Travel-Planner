package com.example.ai_travel_planner.network.models;

import com.google.gson.annotations.SerializedName;

// Đây là class Model (POJO) ở phía Android
public class ItineraryCollaborator {

    @SerializedName("itineraryId")
    private Long itineraryId;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("role")
    private String role; // "EDITOR" hoặc "VIEWER"

    @SerializedName("invitedBy")
    private Long invitedBy;

    @SerializedName("user")
    private User user;

    public Long getItineraryId() {
        return itineraryId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public User getUser() {
        return user;
    }
}