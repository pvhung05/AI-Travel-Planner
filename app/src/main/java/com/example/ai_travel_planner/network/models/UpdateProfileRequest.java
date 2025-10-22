package com.example.ai_travel_planner.network.models;

public class UpdateProfileRequest {
    private String displayName;
    private String email;
    private String password;

    public UpdateProfileRequest(String displayName, String email, String password) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }
}
