package com.example.ai_travel_planner.network.images;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UnsplashApi {
    @GET("search/photos")
    Call<SearchResponse> searchPhotos(
            @Query("query") String query,
            @Query("per_page") int perPage,
            @Query("orientation") String orientation,
            @Query("client_id") String accessKey
    );

    class SearchResponse {
        public int total;
        @SerializedName("total_pages") public int totalPages;
        public List<Result> results;
    }

    class Result {
        public Urls urls;
        public String description;
        @SerializedName("alt_description") public String altDescription;
    }

    class Urls {
        public String small;
        public String regular;
        public String full;
    }
}