package com.example.ai_travel_planner.ui.home.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    public final List<FlightRow> data = new ArrayList<>();
    private FlightRowAdapter adapter;

    // các tham số từ PlaneBookingScreen (đã có)
    private String fromIata, toIata, departISO, returnISO, cabin;
    private int adults;
    private boolean roundTrip;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_results);

        rv = findViewById(R.id.rvFlights);
        progress = findViewById(R.id.progress);
        txtEmpty = findViewById(R.id.txtEmpty);

        fromIata   = getIntent().getStringExtra("from");
        toIata     = getIntent().getStringExtra("to");
        departISO  = getIntent().getStringExtra("depart");
        returnISO  = getIntent().getStringExtra("ret");
        cabin      = getIntent().getStringExtra("cabin");
        adults     = getIntent().getIntExtra("adults", 1);
        roundTrip  = getIntent().getBooleanExtra("roundTrip", false);

        TextView header = findViewById(R.id.txtHeader);
        header.setText(roundTrip && returnISO != null && !returnISO.isEmpty()
                ? String.format("%s → %s  (%s → %s)", fromIata, toIata, departISO, returnISO)
                : String.format("%s → %s  (%s)", fromIata, toIata, departISO));

        adapter = new FlightRowAdapter(data, row -> {
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
            it.putExtra("deepLink", row.deepLink);
            startActivity(it);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // ❶ Ở đây bạn đã có code gọi Amadeus → hãy map sang data.add(row) như dưới.
        //    Nếu cần test nhanh, dùng mockToRows() rồi thay bằng kết quả thật.
        showLoading(true);
        // TODO: thay bằng: parseAmadeusResponseToRows(amadeusJson);
        mockToRows(); // demo
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
        for (int i = 0; i < 4; i++) {
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
            r.currency = "USD";
            r.deepLink = buildBookingLink(fromIata, toIata, departISO,
                    roundTrip ? returnISO : null, adults, r.airline, r.flightNo);
            data.add(r);
        }
        adapter.notifyDataSetChanged();
    }

    /** Gợi ý deeplink (Google Flights) từ thông tin Amadeus */
    public static String buildBookingLink(String from, String to, String departISO,
                                          String returnISO, int adults,
                                          String airline, String flightNo) {
        // Cách ổn định: query search của Google Flights (không phụ thuộc partner)
        String q = "Flights from " + from + " to " + to + " on " + departISO;
        if (returnISO != null && !returnISO.isEmpty()) q += " returning " + returnISO;
        if (airline != null && !airline.isEmpty()) q += " " + airline;
        if (flightNo != null && !flightNo.isEmpty()) q += " " + flightNo;
        // encode nhẹ — đủ dùng
        q = q.replace(" ", "%20");
        return "https://www.google.com/travel/flights?q=" + q;
    }

    // ===== model cho 1 dòng =====
    public static class FlightRow {
        public String airline, flightNo, from, to, depTime, arrTime, currency;
        public double price;
        public int    stops;
        public String duration, baggage;
        public String deepLink; // mở khi Book now
    }
}