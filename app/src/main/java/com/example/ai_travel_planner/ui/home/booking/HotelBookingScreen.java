package com.example.ai_travel_planner.ui.home.booking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.BuildConfig;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Màn hình đặt khách sạn: sử dụng Geoapify để chọn điểm đến,
 * chọn ngày và số khách - KHÔNG dùng Google Places.
 */
public class HotelBookingScreen extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtDestination, txtDate, txtGuests;

    // Dữ liệu người dùng chọn
    private String destinationName;
    private Double lat, lng;
    private Long checkInEpoch = null, checkOutEpoch = null;
    private int rooms = 1, adults = 2, children = 0;

    // Nhận kết quả từ PlaceSearchActivity (Geoapify)
    private final androidx.activity.result.ActivityResultLauncher<Intent> placeLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    destinationName = result.getData().getStringExtra("name");
                    lat = result.getData().getDoubleExtra("lat", 0);
                    lng = result.getData().getDoubleExtra("lng", 0);
                    txtDestination.setText(destinationName);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_booking);
        android.util.Log.d("GEO_KEY", "Geoapify key=" + com.example.ai_travel_planner.BuildConfig.GEOAPIFY_KEY);
        btnBack = findViewById(R.id.btnBack);
        txtDestination = findViewById(R.id.txtDestination);
        txtDate = findViewById(R.id.txtDate);
        txtGuests = findViewById(R.id.txtGuests);

        btnBack.setOnClickListener(v -> finish());

        // Mở activity tìm địa điểm (Geoapify)
        txtDestination.setOnClickListener(v -> openPlacesGeoapify());

        // Chọn ngày nhận & trả phòng
        txtDate.setOnClickListener(v -> openDateRangePicker());

        // Chọn số lượng khách
        txtGuests.setOnClickListener(v -> openGuestsBottomSheet());

        // Khi nhấn Search
        findViewById(R.id.btnSearch).setOnClickListener(v -> doSearch());
    }

    /** Mở PlaceSearchActivity để chọn địa điểm qua Geoapify */
    private void openPlacesGeoapify() {
        String key = BuildConfig.GEOAPIFY_KEY;
        if (key == null || key.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu GEOAPIFY_KEY. Vui lòng thêm key vào local.properties", Toast.LENGTH_LONG).show();
            return;
        }
        Intent it = new Intent(this, PlaceSearchActivity.class);
        placeLauncher.launch(it);
    }

    /** Hiển thị chọn khoảng ngày */
    private void openDateRangePicker() {
        MaterialDatePicker.Builder<?> builder = MaterialDatePicker.Builder.dateRangePicker();
        MaterialDatePicker<?> picker = builder.setTitleText("Select Date").build();
        picker.addOnPositiveButtonClickListener(selection -> {
            try {
                @SuppressWarnings("unchecked")
                androidx.core.util.Pair<Long, Long> pair = (androidx.core.util.Pair<Long, Long>) selection;
                checkInEpoch = pair.first;
                checkOutEpoch = pair.second;
                txtDate.setText(formatRange(checkInEpoch, checkOutEpoch));
            } catch (Exception ignored) {}
        });
        picker.show(getSupportFragmentManager(), "date_range");
    }

    /** Format hiển thị khoảng ngày */
    private String formatRange(long start, long end) {
        SimpleDateFormat f = new SimpleDateFormat("EEE, dd/MM", Locale.getDefault());
        return f.format(new Date(start)) + " - " + f.format(new Date(end));
    }

    /** Mở bottom sheet để chọn số khách và phòng */
    private void openGuestsBottomSheet() {
        GuestsBottomSheet bs = new GuestsBottomSheet((r, a, c) -> {
            rooms = r; adults = a; children = c;
            txtGuests.setText(rooms + " Room, " + (adults + children) + " Guest");
        });
        bs.show(getSupportFragmentManager(), "guests");
    }

    /** Xử lý khi nhấn Search */
    private void doSearch() {
        if (destinationName == null || destinationName.trim().isEmpty()) {
            Toast.makeText(this, "Chọn điểm đến", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkInEpoch == null || checkOutEpoch == null || checkInEpoch >= checkOutEpoch) {
            Toast.makeText(this, "Chọn ngày hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(this, HotelSearchResultScreen.class);
        it.putExtra("destinationName", destinationName);
        it.putExtra("lat", lat);      // PHẢI có
        it.putExtra("lng", lng);      // PHẢI có
        it.putExtra("checkInEpoch", checkInEpoch);
        it.putExtra("checkOutEpoch", checkOutEpoch);
        it.putExtra("rooms", rooms);
        it.putExtra("adults", adults);
        it.putExtra("children", children);
        startActivity(it);
    }
}