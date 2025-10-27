package com.example.ai_travel_planner.ui.home.booking.net.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** booking-com15 trả về: { "data": [ { ...hotel fields... } ] } */
public class HotelSearchResponse {
    @SerializedName("data")
    public List<Hotel> data;

    public static class Hotel {
        @SerializedName("hotel_name")       public String hotel_name;
        @SerializedName("address")          public String address;
        @SerializedName("review_score")     public Double review_score;    // 0..10
        @SerializedName("min_total_price")  public Integer min_total_price;
        @SerializedName("property_photo")   public String property_photo;   // thumb
        @SerializedName("city")             public String city;
        @SerializedName("country_trans")    public String country_trans;
    }
}