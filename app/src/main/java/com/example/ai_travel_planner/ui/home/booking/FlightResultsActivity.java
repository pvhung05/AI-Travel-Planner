package com.example.ai_travel_planner.ui.home.booking;

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

/**
 * Hiển thị danh sách offer. Tạm dùng dữ liệu giả lập để bạn test UI ngay.
 * Khi tích hợp API thật, chỉ cần sửa hàm loadOffers() để fetch & parse.
 */
public class FlightResultsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar loading;
    private TextView empty;

    private final List<FlightRow> data = new ArrayList<>();
    private FlightRowAdapter adapter;

    private String from, to, depart, ret;
    private int adults;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_results);

        rv = findViewById(R.id.rvFlights);
        loading = findViewById(R.id.progress);
        empty = findViewById(R.id.txtEmpty);

        from   = getIntent().getStringExtra("from");
        to     = getIntent().getStringExtra("to");
        depart = getIntent().getStringExtra("depart");
        ret    = getIntent().getStringExtra("ret");
        adults = getIntent().getIntExtra("adults", 1);

        TextView header = findViewById(R.id.txtHeader);
        header.setText(ret == null || ret.isEmpty()
                ? String.format("%s → %s  (%s)", from, to, depart)
                : String.format("%s → %s  (%s → %s)", from, to, depart, ret));

        adapter = new FlightRowAdapter(data);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadOffers(); // TODO: thay bằng API thật
    }

    // ====== Replace bằng call API thật khi bạn sẵn sàng ======
    private void loadOffers() {
        loading.setVisibility(View.VISIBLE);
        rv.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);

        // fake 8 items
        data.clear();
        for (int i = 0; i < 8; i++) {
            FlightRow row = new FlightRow();
            row.airline = (i % 2 == 0) ? "Vietnam Airlines" : "VietJet Air";
            row.flightNo = (i % 2 == 0) ? "VN" + (200 + i) : "VJ" + (600 + i);
            row.from = from; row.to = to;
            row.depTime = depart + " 08:" + (i + 1) + "0";
            row.arrTime = depart + " 10:" + (i + 2) + "5";
            row.price = 100 + i * 7;
            row.currency = "USD";
            data.add(row);
        }

        adapter.notifyDataSetChanged();
        loading.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
        empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Model đơn giản cho một dòng
    public static class FlightRow {
        public String airline;
        public String flightNo;
        public String from;
        public String to;
        public String depTime;
        public String arrTime;
        public double price;
        public String currency;
    }
}