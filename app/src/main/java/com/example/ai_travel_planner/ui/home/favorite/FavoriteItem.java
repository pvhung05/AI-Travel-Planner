package com.example.ai_travel_planner.ui.home.favorite;

public class FavoriteItem {
    private String name;
    private String address;
    private float rating;
    private int imageRes;

    public FavoriteItem(String name, String address, float rating, int imageRes) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public float getRating() { return rating; }
    public int getImageRes() { return imageRes; }
}
