// app/src/main/java/com/example/ai_travel_planner/network/models/GeoResponse.java
package com.example.ai_travel_planner.network.models;

import java.util.List;

public class GeoResponse {
    public String type;
    public List<Feature> features; // Danh sách các Feature

    public static class Feature {
        public String type;
        public Properties properties; // Đối tượng Properties
        public Geometry geometry;     // Đối tượng Geometry
    }

    public static class Properties {
        public String name;
        public String city;
        public String country;
        public String address_line1;
        public String address_line2;
        // ... (các trường khác như postcode, county, state...)

        public List<String> categories;

        public Datasource datasource; // Thêm nếu cần thông tin nguồn

        public Double rating;

        // Thêm lớp Datasource nếu cần
        public static class Datasource {
            public String sourcename;
            public String attribution;
            public String license;
            public String url;
            public Raw raw;
            public static class Raw {
                public String amenity;
                public String name;
                // ... các trường raw khác
            }
        }
    }

    public static class Geometry {
        public String type;
        public List<Double> coordinates; // Thường là [longitude, latitude]
    }
}