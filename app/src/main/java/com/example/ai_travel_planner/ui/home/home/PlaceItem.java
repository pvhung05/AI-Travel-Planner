package com.example.ai_travel_planner.ui.home.home;

public class PlaceItem {
    private String name;
    private String location;
    private int imageResId; // ID ảnh (trong drawable)

    public PlaceItem(String name, String location, double price, int imageResId) {
        this.name = name;
        this.location = location;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getImageResId() {
        return imageResId;
    }

    // Setters (nếu cần)
    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
