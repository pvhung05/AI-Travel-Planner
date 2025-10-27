package com.example.ai_travel_planner.ui.home.booking.net;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class GeoapifyResponse {
    @SerializedName("features")
    public List<Feature> features = new ArrayList<>();

    public static class Feature {
        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("properties")
        public Properties properties;
    }

    public static class Geometry {
        @SerializedName("type")
        public String type;

        // GeoJSON: [lon, lat]
        @SerializedName("coordinates")
        public List<Double> coordinates;
    }

    public static class Properties {
        @SerializedName("name")
        public String name;

        @SerializedName("formatted")
        public String formatted;

        @SerializedName("city")
        public String city;

        @SerializedName("state")
        public String state;

        @SerializedName("country")
        public String country;

        @SerializedName("lat")
        public Double lat;

        @SerializedName("lon")
        public Double lon;

        // Fallback khi không có name/formatted
        @SerializedName("address_line1")
        public String address_line1;

        @SerializedName("address_line2")
        public String address_line2;
    }
}