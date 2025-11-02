package com.example.ai_travel_planner.network.models;

public class AiPlanRequest {
    public String destination;
    public int days;
    public String style;   // "ẩm thực, thiên nhiên"...
    public double budget;  // VND

    public AiPlanRequest(String destination, int days, String style, double budget) {
        this.destination = destination;
        this.days = days;
        this.style = style;
        this.budget = budget;
    }

    public String getDestination() {
        return destination;
    }

    public int getNumDays() {
        return days;
    }

    public String getStyle() {
        return style;
    }

    public double getBudget() {
        return budget;
    }
}