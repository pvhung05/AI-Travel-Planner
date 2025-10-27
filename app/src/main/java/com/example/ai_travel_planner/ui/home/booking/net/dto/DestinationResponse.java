package com.example.ai_travel_planner.ui.home.booking.net.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** booking-com15 trả về: { "data": [ { "dest_id": "...", "name": "...", ... } ] } */
public class DestinationResponse {
    @SerializedName("data")
    public List<Destination> data;

    public static class Destination {
        @SerializedName("dest_id")      public String dest_id;
        @SerializedName("dest_type")    public String dest_type;   // CITY / REGION ...
        @SerializedName("name")         public String name;
        @SerializedName("country")      public String country;
        @SerializedName("city_name")    public String city_name;
        @SerializedName("label")        public String label;
        @SerializedName("latitude")     public Double latitude;
        @SerializedName("longitude")    public Double longitude;
    }
}