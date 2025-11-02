    package com.example.ai_travel_planner.network;

    import com.example.ai_travel_planner.network.models.AiPlanResponse;
    import com.example.ai_travel_planner.network.models.CreateItineraryRequest;
    import com.example.ai_travel_planner.network.models.InviteLinkResponse;
    import com.example.ai_travel_planner.network.models.ItineraryCollaborator;
    import com.example.ai_travel_planner.network.models.ItineraryResponse;
    import com.example.ai_travel_planner.network.models.LoginRequest;
    import com.example.ai_travel_planner.network.models.LoginResponse;
    import com.example.ai_travel_planner.network.models.RegisterRequest;
    import com.example.ai_travel_planner.network.models.RegisterResponse;
    import com.example.ai_travel_planner.network.models.UserMe;
    import com.example.ai_travel_planner.ui.home.plan.PlaceItem;


    import java.util.List;

    import okhttp3.MultipartBody;
    import okhttp3.RequestBody;
    import retrofit2.Call;
    import retrofit2.http.Body;
    import retrofit2.http.GET;
    import retrofit2.http.Header;
    import retrofit2.http.Multipart;
    import retrofit2.http.PATCH;
    import retrofit2.http.POST;
    import retrofit2.http.PUT;
    import retrofit2.http.Part;
    import retrofit2.http.Path;
    import retrofit2.http.Query;

    public interface ApiService {

        @POST("/api/auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);

        @POST("api/auth/register")
        Call<RegisterResponse> register(@Body RegisterRequest request);

        @GET("/api/user/me")
        Call<UserMe> getMe(@Header("Authorization") String bearerToken);

        @Multipart
        @PUT("/api/user/profile")
        Call<UserMe> updateProfile(@Header("Authorization") String authToken,
                                   @Part("displayName") RequestBody displayName,
                                   @Part("email") RequestBody email,
                                   @Part("password") RequestBody password,
                                   @Part MultipartBody.Part avatarUrl
        );

        @GET("/api/places/most-viewed")
        Call<List<PlaceItem>> getMostViewedPlaces();

        @GET("/api/places/latest")
        Call<List<PlaceItem>> getLatestPlaces();

        // THÊM API TẠO CHUYẾN ĐI (từ bước trước)
        @POST("/api/itineraries")
        Call<ItineraryResponse> createItinerary(
                @Header("Authorization") String bearerToken, // <-- Phải có
                @Body CreateItineraryRequest request
        );

        // SỬA LẠI: Thêm @Header("Authorization")
        @GET("/api/itineraries/{itineraryId}/invite-link")
        Call<InviteLinkResponse> getInviteLink(
                @Header("Authorization") String bearerToken, // <-- THÊM VÀO
                @Path("itineraryId") long itineraryId
        );

        // SỬA LẠI: Thêm @Header("Authorization")
        @POST("/api/itineraries/{itineraryId}/collaborators/invite")
        Call<ItineraryCollaborator> inviteCollaborator(
                @Header("Authorization") String bearerToken, // <-- THÊM VÀO
                @Path("itineraryId") long itineraryId,
                @Query("email") String email,
                @Query("role") String role
        );

        @GET("/api/itineraries/{itineraryId}/collaborators")
        Call<List<ItineraryCollaborator>> getCollaborators(
                @Header("Authorization") String bearerToken,
                @Path("itineraryId") long itineraryId
        );
    }
