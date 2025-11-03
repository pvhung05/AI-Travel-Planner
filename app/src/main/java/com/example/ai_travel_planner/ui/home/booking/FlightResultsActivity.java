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
import com.example.ai_travel_planner.net.amadeus.AmadeusClient;
import com.example.ai_travel_planner.net.amadeus.AmadeusService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Gọi API thật để lấy danh sách chuyến bay
        fetchFlightOffers();
    }

    private void showLoading(boolean s) {
        progress.setVisibility(s ? View.VISIBLE : View.GONE);
        rv.setVisibility(s ? View.GONE : View.VISIBLE);
        txtEmpty.setVisibility(!s && data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void fetchFlightOffers() {
        // Chuẩn hóa & kiểm tra IATA 3 ký tự
        String origin = fromIata == null ? "" : fromIata.trim().toUpperCase();
        String dest   = toIata   == null ? "" : toIata.trim().toUpperCase();
        if (!origin.matches("[A-Z]{3}") || !dest.matches("[A-Z]{3}")) {
            txtEmpty.setText("Origin/Destination phải là mã IATA 3 ký tự (vd: HAN, SGN).");
            txtEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            return;
        }

        showLoading(true);

        AmadeusService svc = AmadeusClient.authed().create(AmadeusService.class);
        Call<JsonObject> call = svc.searchFlightOffers(
                origin,
                dest,
                departISO,
                (roundTrip && returnISO != null && !returnISO.isEmpty()) ? returnISO : null,
                adults,
                currency
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                data.clear();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject body = response.body();
                        JsonArray offers = body.has("data") && body.get("data").isJsonArray()
                                ? body.getAsJsonArray("data") : new JsonArray();

                        for (JsonElement el : offers) {
                            if (!el.isJsonObject()) continue;
                            JsonObject offer = el.getAsJsonObject();

                            // price
                            double totalPrice = 0.0;
                            if (offer.has("price") && offer.get("price").isJsonObject()) {
                                JsonObject priceObj = offer.getAsJsonObject("price");
                                if (priceObj.has("grandTotal")) {
                                    try { totalPrice = Double.parseDouble(priceObj.get("grandTotal").getAsString()); } catch (Exception ignored) {}
                                } else if (priceObj.has("total")) {
                                    try { totalPrice = Double.parseDouble(priceObj.get("total").getAsString()); } catch (Exception ignored) {}
                                }
                            }

                            // itineraries/segments → lấy chặng đầu và cuối (để hiển thị giờ đi/đến, hãng, số hiệu)
                            String airline = "";
                            String flightNo = "";
                            String depTs = "";
                            String arrTs = "";
                            int stops = 0;
                            String duration = offer.has("itineraries") && offer.get("itineraries").isJsonArray()
                                    ? offer.getAsJsonArray("itineraries").size() > 0 && offer.getAsJsonArray("itineraries").get(0).isJsonObject()
                                        ? offer.getAsJsonArray("itineraries").get(0).getAsJsonObject().has("duration")
                                            ? offer.getAsJsonArray("itineraries").get(0).getAsJsonObject().get("duration").getAsString()
                                            : ""
                                        : ""
                                    : "";

                            if (offer.has("itineraries") && offer.get("itineraries").isJsonArray()) {
                                JsonArray itins = offer.getAsJsonArray("itineraries");
                                if (itins.size() > 0 && itins.get(0).isJsonObject()) {
                                    JsonObject firstItin = itins.get(0).getAsJsonObject();
                                    if (firstItin.has("segments") && firstItin.get("segments").isJsonArray()) {
                                        JsonArray segs = firstItin.getAsJsonArray("segments");
                                        stops = Math.max(0, segs.size() - 1);
                                        if (segs.size() > 0 && segs.get(0).isJsonObject()) {
                                            JsonObject firstSeg = segs.get(0).getAsJsonObject();
                                            airline = firstSeg.has("carrierCode") ? firstSeg.get("carrierCode").getAsString() : "";
                                            String number = firstSeg.has("number") ? firstSeg.get("number").getAsString() : "";
                                            flightNo = (airline + number).trim();
                                            depTs = firstSeg.has("departure") && firstSeg.get("departure").isJsonObject()
                                                    ? firstSeg.getAsJsonObject("departure").has("at")
                                                        ? firstSeg.getAsJsonObject("departure").get("at").getAsString() : ""
                                                    : "";
                                        }
                                        if (segs.size() > 0 && segs.get(segs.size()-1).isJsonObject()) {
                                            JsonObject lastSeg = segs.get(segs.size()-1).getAsJsonObject();
                                            arrTs = lastSeg.has("arrival") && lastSeg.get("arrival").isJsonObject()
                                                    ? lastSeg.getAsJsonObject("arrival").has("at")
                                                        ? lastSeg.getAsJsonObject("arrival").get("at").getAsString() : ""
                                                    : "";
                                        }
                                    }
                                }
                            }

                            FlightRow r = new FlightRow();
                            r.airline  = airline;
                            r.flightNo = flightNo;
                            r.from     = fromIata;
                            r.to       = toIata;
                            r.depTime  = depTs;
                            r.arrTime  = arrTs;
                            r.stops    = stops;
                            r.duration = duration;
                            r.baggage  = ""; // có thể lấy từ pricing/fees nếu cần
                            r.price    = totalPrice;
                            r.currency = currency;
                            data.add(r);
                        }
                    } else {
                        String err = "";
                        try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored2) {}
                        txtEmpty.setText("Lỗi " + response.code() + (err.isEmpty() ? "" : ("\n" + err)));
                        txtEmpty.setVisibility(View.VISIBLE);
                    }
                } catch (Exception ignored) { }

                adapter.notifyDataSetChanged();
                showLoading(false);
            }

            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                data.clear();
                adapter.notifyDataSetChanged();
                showLoading(false);
                txtEmpty.setText("Không thể tải kết quả.\n" + (t != null ? t.getMessage() : ""));
                txtEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    // ===== model cho 1 dòng =====
    public static class FlightRow {
        public String airline, flightNo, from, to, depTime, arrTime, currency;
        public double price;
        public int    stops;
        public String duration, baggage;
    }
}