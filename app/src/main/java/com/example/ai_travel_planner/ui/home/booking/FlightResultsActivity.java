package com.example.ai_travel_planner.ui.home.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;

import java.util.ArrayList;
import java.util.List;

public class FlightResultsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView txtEmpty;
    private ImageButton btnBack;

    public final List<FlightRow> data = new ArrayList<>();
    private FlightRowAdapter adapter;

    // nhận từ PlaneBookingScreen
    private String fromIata, toIata, departISO, returnISO, cabin, currency;
    private int adults;
    private boolean roundTrip;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_results);

        rv = findViewById(R.id.rvFlights);
        progress = findViewById(R.id.progress);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // ✅ ĐỌC ĐÚNG KEYS TỪ PlaneBookingScreen
        fromIata   = getIntent().getStringExtra("from");
        toIata     = getIntent().getStringExtra("to");
        departISO  = getIntent().getStringExtra("departISO");
        returnISO  = getIntent().getStringExtra("returnISO");
        cabin      = getIntent().getStringExtra("cabin");     if (cabin == null) cabin = "ECONOMY";
        adults     = Math.max(1, getIntent().getIntExtra("adults", 1));
        roundTrip  = getIntent().getBooleanExtra("roundTrip", true);
        currency   = getIntent().getStringExtra("currency");  if (currency == null) currency = "USD";

        TextView header = findViewById(R.id.txtHeader);
        header.setText(roundTrip && returnISO != null && !returnISO.isEmpty()
                ? String.format("%s → %s  (%s → %s)", fromIata, toIata, departISO, returnISO)
                : String.format("%s → %s  (%s)", fromIata, toIata, departISO));

        adapter = new FlightRowAdapter(data, row -> {
            // ➜ Mở màn chi tiết & TRUYỀN THAM SỐ để deep-link auto-fill
            Intent it = new Intent(this, FlightDetailActivity.class);
            it.putExtra("airline",  row.airline);
            it.putExtra("flightNo", row.flightNo);
            it.putExtra("from",     row.from);
            it.putExtra("to",       row.to);
            it.putExtra("depTime",  row.depTime);
            it.putExtra("arrTime",  row.arrTime);
            it.putExtra("price",    row.price);
            it.putExtra("currency", row.currency);
            it.putExtra("stops",    row.stops);
            it.putExtra("duration", row.duration);
            it.putExtra("baggage",  row.baggage);

            // các tham số để BookingDeepLinks build URL prefill
            it.putExtra("fromIata",  fromIata);
            it.putExtra("toIata",    toIata);
            it.putExtra("departISO", departISO);
            it.putExtra("returnISO", roundTrip ? returnISO : "");
            it.putExtra("adults",    adults);
            it.putExtra("cabin",     cabin);
            it.putExtra("roundTrip", roundTrip);
            startActivity(it);
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // TODO: thay mockToRows() bằng parse kết quả Amadeus thật của bạn.
        showLoading(true);
        mockToRows();
        showLoading(false);
    }

    private void showLoading(boolean s) {
        progress.setVisibility(s ? View.VISIBLE : View.GONE);
        rv.setVisibility(s ? View.GONE : View.VISIBLE);
        txtEmpty.setVisibility(!s && data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ==== DEMO ONLY: Xoá khi đã map từ Amadeus ====
    private void mockToRows() {
        data.clear();
        for (int i = 0; i < 6; i++) {
            FlightRow r = new FlightRow();
            r.airline  = (i % 2 == 0) ? "Vietnam Airlines" : "Bamboo Airways";
            r.flightNo = (i % 2 == 0) ? "VN" + (200 + i) : "QH" + (110 + i);
            r.from     = fromIata; r.to = toIata;
            r.depTime  = departISO + " 08:" + (i+1) + "0";
            r.arrTime  = departISO + " 10:" + (i+2) + "0";
            r.stops    = 0;
            r.duration = "2h 10m";
            r.baggage  = "7kg cabin";
            r.price    = 120 + i * 10;
            r.currency = currency;
            data.add(r);
        }
        adapter.notifyDataSetChanged();
    }

    // ===== model cho 1 dòng =====
    public static class FlightRow {
        public String airline, flightNo, from, to, depTime, arrTime, currency;
        public double price;
        public int    stops;
        public String duration, baggage;
    }
}