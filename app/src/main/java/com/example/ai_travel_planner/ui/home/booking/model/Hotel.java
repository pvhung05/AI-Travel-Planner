package com.example.ai_travel_planner.ui.home.booking.model;

import java.io.Serializable;

public class Hotel implements Serializable {
    public String name;
    public String address;
    public float rating;
    public double priceMin;
    public String thumbnailUrl;

    // Thêm các field hữu ích nếu API trả về:
    public String providerDeepLink; // ví dụ: link tới trang khách sạn (nếu có)
    public Double lat;              // toạ độ khách sạn (nếu có)
    public Double lon;              // toạ độ khách sạn (nếu có)
    public String city;             // thành phố (fallback build URL)
}