package com.example.ai_travel_planner.network.models;

import java.util.List;

public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public Wind wind;

    public static class Main {
        public double temp;
        public double feels_like;
        public int humidity;
    }

    public static class Weather {
        public String main; // "Clouds", "Rain"
        public String description;
        public String icon;
    }

    public static class Wind {
        public double speed;
    }
}