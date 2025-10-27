package com.example.ai_travel_planner.ui.home.booking.net;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BookingApi {

    // Search theo toạ độ (KHÔNG cần dest_id)
    @GET("hotels/search-by-coordinates")
    Call<HotelByCoordResponse> searchByCoordinates(
            @Query("checkout_date") String checkoutDate,   // yyyy-MM-dd
            @Query("checkin_date")  String checkinDate,    // yyyy-MM-dd
            @Query("adults_number") int adultsNumber,
            @Query("room_number")   int roomNumber,
            @Query("order_by")      String orderBy,        // "popularity"
            @Query("units")         String units,          // "metric"
            @Query("locale")        String locale,         // "en-gb"
            @Query("longitude")     double longitude,
            @Query("latitude")      double latitude,
            @Query("filter_by_currency") String currency,  // "USD"
            @Query("page_number")   int page,              // 0
            @Query("include_adjacency") String includeAdj  // "true"
    );

    /* --------- Response model tối giản --------- */
    class HotelByCoordResponse {
        public List<Result> result;
        public static class Result {
            @SerializedName("hotel_name") public String hotelName;
            public String address;
            @SerializedName("review_score")   public Double reviewScore;
            @SerializedName("min_total_price")public Double minTotalPrice;
            @SerializedName("main_photo_url") public String mainPhotoUrl;
        }
    }
}