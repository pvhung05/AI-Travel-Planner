package com.example.ai_travel_planner.ui.home.booking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.booking.model.Hotel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HotelDetailScreen extends AppCompatActivity {

    private ImageView imgThumb;
    private TextView tvName, tvAddr, tvRating, tvPrice;
    private Button btnBookNow;
    private ImageButton btnBack;

    private Hotel hotel;
    private long checkInEpoch, checkOutEpoch;
    private int adults, rooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        imgThumb = findViewById(R.id.imgThumb);
        tvName   = findViewById(R.id.tvName);
        tvAddr   = findViewById(R.id.tvAddr);
        tvRating = findViewById(R.id.tvRating);
        tvPrice  = findViewById(R.id.tvPrice);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnBack = findViewById(R.id.btnBack);

        readExtras();
        bindUI();

        btnBookNow.setOnClickListener(v -> onBookNow());
        btnBack.setOnClickListener(v -> finish());
    }

    private void readExtras() {
        Intent it = getIntent();
        Object o = it.getSerializableExtra("hotel");
        if (o instanceof Hotel) {
            hotel = (Hotel) o;
        }
        checkInEpoch = it.getLongExtra("checkInEpoch", 0);
        checkOutEpoch= it.getLongExtra("checkOutEpoch",0);
        adults       = it.getIntExtra("adults", 2);
        rooms        = it.getIntExtra("rooms", 1);

        if (hotel == null) {
            Toast.makeText(this, "Thiếu dữ liệu khách sạn", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void bindUI() {
        tvName.setText(nullToEmpty(hotel.name));
        tvAddr.setText(nullToEmpty(hotel.address));
        tvRating.setText("Rating: " + hotel.rating);
        tvPrice.setText(hotel.priceMin > 0 ? ("From: " + formatMoney(hotel.priceMin) + " VND") : "Price: N/A");

        Glide.with(this)
                .load(hotel.thumbnailUrl)
                .placeholder(R.drawable.sample_mountain)
                .centerCrop()
                .into(imgThumb);
    }

    private String formatMoney(double v) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(v);
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }

    private void onBookNow() {
        // Ưu tiên deep link từ API
        if (!TextUtils.isEmpty(hotel.providerDeepLink)) {
            openUrl(hotel.providerDeepLink);
            return;
        }

        // Fallback #1: Tự build link Booking Search (tìm theo tên + ngày)
        String checkin  = toDate(checkInEpoch);
        String checkout = toDate(checkOutEpoch);
        String query    = Uri.encode(hotel.name + " " + nullToEmpty(hotel.city));
        // Một dạng URL search trên booking:
        String bookingSearch = "https://www.booking.com/searchresults.html?ss=" + query
                + "&checkin_year_month_monthday=" + checkin
                + "&checkout_year_month_monthday=" + checkout
                + "&group_adults=" + Math.max(1, adults)
                + "&no_rooms=" + Math.max(1, rooms);

        openUrl(bookingSearch);
    }

    private String toDate(long epoch) {
        if (epoch <= 0) return "";
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(epoch));
    }

    private void openUrl(String url) {
        try {
            Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(it);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được trang đặt phòng", Toast.LENGTH_LONG).show();
        }
    }
}