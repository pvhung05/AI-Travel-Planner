package com.example.ai_travel_planner.ui.home.plan;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PlaceItem implements Serializable {
    private String name;
    private String vicinity;
    private String imageUrl;

    private Double rating;

    private String description;

    @SerializedName("lat")
    private Double latitude;

    @SerializedName("lng")
    private Double longitude;



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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}