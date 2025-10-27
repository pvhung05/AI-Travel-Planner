package com.example.ai_travel_planner.ui.home.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.booking.model.Hotel;
import com.example.ai_travel_planner.ui.home.booking.net.BookingApi;
import com.example.ai_travel_planner.ui.home.booking.net.Net;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelSearchResultScreen extends AppCompatActivity {

    private TextView tvHeader, tvEmpty;
    private RecyclerView rvHotels;
    private ProgressBar progress;
    private HotelsAdapter adapter;

    String destinationName;
    long checkInEpoch, checkOutEpoch;
    int rooms, adults, children;
    Double lat, lng; // NHỚ: HotelBookingScreen truyền qua lat/lng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_search_result);

        tvHeader  = findViewById(R.id.tvHeader);
        tvEmpty   = findViewById(R.id.tvEmpty);
        rvHotels  = findViewById(R.id.rvHotels);
        progress  = findViewById(R.id.progress);

        readInputParams();
        tvHeader.setText(destinationName + " • " + formatNights());

        rvHotels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HotelsAdapter(hotel -> {
            Intent it = new Intent(this, HotelDetailScreen.class);
            it.putExtra("hotel", hotel); // Serializable

            it.putExtra("checkInEpoch", checkInEpoch);
            it.putExtra("checkOutEpoch", checkOutEpoch);
            it.putExtra("adults", adults);
            it.putExtra("rooms", rooms);

            startActivity(it);
        });
        rvHotels.setAdapter(adapter);

        fetchHotelsByCoordinates();
    }

    private void readInputParams() {
        Intent it = getIntent();
        destinationName = it.getStringExtra("destinationName");
        checkInEpoch    = it.getLongExtra("checkInEpoch", 0);
        checkOutEpoch   = it.getLongExtra("checkOutEpoch", 0);
        rooms           = it.getIntExtra("rooms", 1);
        adults          = it.getIntExtra("adults", 2);
        children        = it.getIntExtra("children", 0);
        if (it.hasExtra("lat")) lat = it.getDoubleExtra("lat", 0);
        if (it.hasExtra("lng")) lng = it.getDoubleExtra("lng", 0);
    }

    private String formatNights() {
        long nights = Math.max(1, (checkOutEpoch - checkInEpoch) / (1000L * 60 * 60 * 24));
        return nights + " nights • " + (adults + children) + " guests";
    }

    private String toDate(long epoch) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(epoch));
    }

    private void showLoading(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.GONE);
        rvHotels.setVisibility(b ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void fetchHotelsByCoordinates() {
        if (lat == null || lng == null || (lat == 0 && lng == 0)) {
            showEmpty("Thiếu toạ độ để tìm khách sạn");
            return;
        }
        showLoading(true);

        Net.booking().searchByCoordinates(
                /* checkout */ toDate(checkOutEpoch),
                /* checkin  */ toDate(checkInEpoch),
                /* adults   */ adults,
                /* rooms    */ rooms,
                /* order_by */ "popularity",
                /* units    */ "metric",
                /* locale   */ "en-gb",
                /* lon/lat  */ lng, lat,
                /* currency */ "USD",
                /* page     */ 0,
                /* include  */ "true"
        ).enqueue(new Callback<BookingApi.HotelByCoordResponse>() {
            @Override
            public void onResponse(Call<BookingApi.HotelByCoordResponse> call,
                                   Response<BookingApi.HotelByCoordResponse> resp) {
                showLoading(false);
                if (!resp.isSuccessful() || resp.body() == null || resp.body().result == null) {
                    showEmpty("Không tìm thấy khách sạn nào");
                    return;
                }
                List<Hotel> mapped = new ArrayList<>();
                for (BookingApi.HotelByCoordResponse.Result r : resp.body().result) {
                    Hotel h = new Hotel();
                    h.name        = r.hotelName;
                    h.address     = r.address;
                    h.rating      = r.reviewScore == null ? 0f : r.reviewScore.floatValue();
                    h.priceMin    = r.minTotalPrice == null ? 0 : r.minTotalPrice.intValue();
                    h.thumbnailUrl= r.mainPhotoUrl;
                    mapped.add(h);
                }
                if (mapped.isEmpty()) showEmpty("Không tìm thấy khách sạn nào");
                else adapter.setData(mapped);
            }

            @Override
            public void onFailure(Call<BookingApi.HotelByCoordResponse> call, Throwable t) {
                showLoading(false);
                showEmpty("Lỗi API: " + t.getMessage());
            }
        });
    }

    private void showEmpty(String msg) {
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
        rvHotels.setVisibility(View.GONE);
    }
}