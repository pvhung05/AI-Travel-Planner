package com.example.ai_travel_planner.network.models;

public class UserMe {
    private Long id;
    private String email;
    private String displayName;
    private String phone;
    private Double avgBudget;
    private String preferences;
    private boolean twoFaEnabled;

    private String avatarUrl;

    private String token;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPhone() { return phone; }
    public Double getAvgBudget() { return avgBudget; }
    public String getPreferences() { return preferences; }
    public boolean isTwoFaEnabled() { return twoFaEnabled; }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}