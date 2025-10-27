package com.example.ai_travel_planner.ui.home.plan;

import java.io.Serializable;

public class PlaceItem implements Serializable {
    private String name;
    private String vicinity;
    private String imageUrl;

    public String getName() {
        return name != null ? name : "Unknown Place";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity != null ? vicinity : "No address";
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}