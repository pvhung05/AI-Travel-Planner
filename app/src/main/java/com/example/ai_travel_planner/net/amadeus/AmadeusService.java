package com.example.ai_travel_planner.net.amadeus;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AmadeusService {

    // Autocomplete sân bay/thành phố
    @GET("v1/reference-data/locations")
    Call<JsonObject> searchLocations(
            @Query("subType") String subType,                // "AIRPORT,CITY"
            @Query("keyword") String keyword,               // "hanoi"
            @Query("page[limit]") int limit,                // 8
            @Query("sort") String sort                      // "analytics.travelers.score"
    );

    // Search flight offers
    @GET("v2/shopping/flight-offers")
    Call<JsonObject> searchFlightOffers(
            @Query("originLocationCode") String origin,     // "HAN"
            @Query("destinationLocationCode") String dest,  // "SGN"
            @Query("departureDate") String departISO,       // "2025-11-20"
            @Query("returnDate") String returnISO,          // optional
            @Query("adults") int adults,                    // 1
            @Query("currencyCode") String currency          // "USD" (or "VND" nếu bật)
    );
}