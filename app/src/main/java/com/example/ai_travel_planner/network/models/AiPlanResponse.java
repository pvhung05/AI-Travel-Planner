package com.example.ai_travel_planner.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AiPlanResponse {

    @SerializedName("itineraryId")
    public String itineraryId;
    public String destination;
    public int days;
    public String currency;
    public double estimatedTotalCost;
    public Weather weather;
    public List<DayPlan> itinerary;

    public static class Weather {
        public String period;
        public String overview;
        public List<String> tips;
    }

    public static class DayPlan {
        public int day;
        public List<String> places;
        public List<String> foods;
        public String transportHint;
        public double estCost;
        public String notes;
    }
}