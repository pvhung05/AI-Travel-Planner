package com.example.ai_travel_planner.ui.home.booking;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;

import java.util.ArrayList;
import java.util.List;

public class FlightDetailActivity extends AppCompatActivity {

    // ==== model provider ====
    public static class ProviderLink {
        public String name;
        public String url;
        public double priceFrom;
        public String currency;
        public ProviderLink(String name, String url, double priceFrom, String currency) {
            this.name = name; this.url = url; this.priceFrom = priceFrom; this.currency = currency;
        }
    }

    private String deepSearchUrlGoogle; // vẫn giữ để “Book now” mặc định
    private String from, to, depDate, retDate, cabin, airline, flightNo, currency, duration, baggage, depTime, arrTime;
    private int adults;
    private boolean roundTrip;

    private final List<ProviderLink> providers = new ArrayList<>();
    private ProviderRowAdapter providerAdapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_detail);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvTitle    = findViewById(R.id.tvTitle);
        TextView tvRoute    = findViewById(R.id.tvRoute);
        TextView tvTime     = findViewById(R.id.tvTime);
        TextView tvStops    = findViewById(R.id.tvStops);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvBaggage  = findViewById(R.id.tvBaggage);
        TextView tvPrice    = findViewById(R.id.tvPrice);
        Button btnBook      = findViewById(R.id.btnBook);

        RecyclerView rvProviders = findViewById(R.id.rvProviders);
        rvProviders.setLayoutManager(new LinearLayoutManager(this));
        providerAdapter = new ProviderRowAdapter(providers, this::openProvider);
        rvProviders.setAdapter(providerAdapter);

        // ====== nhận từ Results ======
        airline  = getIntent().getStringExtra("airline");
        flightNo = getIntent().getStringExtra("flightNo");
        from     = getIntent().getStringExtra("from");
        to       = getIntent().getStringExtra("to");
        depTime  = getIntent().getStringExtra("depTime");
        arrTime  = getIntent().getStringExtra("arrTime");
        duration = getIntent().getStringExtra("duration");
        baggage  = getIntent().getStringExtra("baggage");
        double price = getIntent().getDoubleExtra("price", 0);
        currency = getIntent().getStringExtra("currency");

        // bổ sung từ intent nếu bạn truyền qua (tuỳ bạn): adults, cabin, roundTrip, depDate/retDate
        adults   = getIntent().getIntExtra("adults", 1);
        cabin    = getIntent().getStringExtra("cabin");
        roundTrip= getIntent().getBooleanExtra("roundTrip", false);
        depDate  = getIntent().getStringExtra("departISO"); // yyyy-MM-dd
        retDate  = getIntent().getStringExtra("returnISO"); // yyyy-MM-dd (optional)

        tvTitle.setText((airline == null ? "" : airline) + "  " + (flightNo == null ? "" : flightNo));
        tvRoute.setText(from + " → " + to);
        tvTime.setText((depTime == null ? "" : depTime) + " → " + (arrTime == null ? "" : arrTime));
        tvDuration.setText(duration == null ? "" : duration);
        tvBaggage.setText(baggage == null ? "" : baggage);
        tvStops.setText(getIntent().getIntExtra("stops", 0) == 0 ? "Direct" : (getIntent().getIntExtra("stops", 0) + " stops"));

        tvPrice.setText(currency == null || currency.isEmpty()
                ? String.format("%,.0f", price)
                : String.format("%,.0f %s", price, currency));

        btnBack.setOnClickListener(v -> finish());

        // Book now mặc định: Google Flights (search)
        deepSearchUrlGoogle = buildGoogleFlightsLink(from, to, depDate, retDate, adults, cabin, airline, flightNo);
        btnBook.setOnClickListener(v -> openUrl(deepSearchUrlGoogle));

        // ==== Build danh sách provider dạng “metasearch style” ====
        providers.clear();
        providers.add(new ProviderLink("Google Flights",
                deepSearchUrlGoogle, price, currency));

        providers.add(new ProviderLink("Kayak",
                buildKayakLink(from, to, depDate, retDate, adults, cabin), price, currency));

        providers.add(new ProviderLink("Skyscanner",
                buildSkyscannerLink(from, to, depDate, retDate, adults, cabin), price, currency));

        providers.add(new ProviderLink("Expedia",
                buildExpediaLink(from, to, depDate, retDate, adults, cabin), price, currency));

        providers.add(new ProviderLink("Trip.com",
                buildTripComLink(from, to, depDate, retDate, adults, cabin), price, currency));

        providerAdapter.notifyDataSetChanged();
    }

    // ===== Open helper =====
    private void openProvider(ProviderLink p) { openUrl(p.url); }

    private void openUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "Link không khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            CustomTabsIntent tabs = new CustomTabsIntent.Builder().build();
            tabs.launchUrl(this, Uri.parse(url));
        } catch (Exception e) {
            try {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception ex) {
                Toast.makeText(this, "Không mở được link", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ====== Deeplink builders (prefill tham số) ======

    private static String enc(String s) { return s == null ? "" : s.replace(" ", "%20"); }

    // Google Flights: dùng truy vấn tự do, dễ match itinerary
    private static String buildGoogleFlightsLink(String from, String to, String d1, String d2, int adults, String cabin, String airline, String flightNo) {
        String q = "Flights from " + from + " to " + to + " on " + d1;
        if (d2 != null && !d2.isEmpty()) q += " returning " + d2;
        if (airline != null && !airline.isEmpty()) q += " " + airline;
        if (flightNo != null && !flightNo.isEmpty()) q += " " + flightNo;
        q += " adults " + Math.max(1, adults);
        if (cabin != null && !cabin.isEmpty()) q += " " + cabin;
        return "https://www.google.com/travel/flights?q=" + enc(q);
    }

    // Kayak: /flights/LHR-DXB/2025-11-21[/2025-11-28]?sort=bestflight_a&adults=1&cabin=e
    private static String buildKayakLink(String from, String to, String d1, String d2, int adults, String cabin) {
        String path = "https://www.kayak.com/flights/" + from + "-" + to + "/" + d1;
        if (d2 != null && !d2.isEmpty()) path += "/" + d2;
        String cb = mapCabinKayak(cabin);
        return path + "?sort=bestflight_a&adults=" + Math.max(1, adults) + "&cabin=" + cb;
    }
    private static String mapCabinKayak(String cabin) {
        if (cabin == null) return "e";
        cabin = cabin.toUpperCase();
        switch (cabin) {
            case "PREMIUM_ECONOMY": return "w";
            case "BUSINESS": return "c";
            case "FIRST": return "f";
            default: return "e";
        }
    }

    // Skyscanner: /transport/flights/LHR/DXB/2025-11-21[/2025-11-28]/?adults=1&cabinclass=economy
    private static String buildSkyscannerLink(String from, String to, String d1, String d2, int adults, String cabin) {
        String base = "https://www.skyscanner.net/transport/flights/" + from + "/" + to + "/" + d1;
        if (d2 != null && !d2.isEmpty()) base += "/" + d2;
        String cb = (cabin == null) ? "economy" : cabin.toLowerCase();
        return base + "/?adults=" + Math.max(1, adults) + "&cabinclass=" + cb;
    }

    // Expedia (EN): Flights-Search?trip=oneway/roundtrip&leg1=from:LHR,to:DXB,departure:2025-11-21TANYT&leg2=...&passengers=adults:1&options=cabinclass:economy
    private static String buildExpediaLink(String from, String to, String d1, String d2, int adults, String cabin) {
        String trip = (d2 != null && !d2.isEmpty()) ? "roundtrip" : "oneway";
        String leg1 = "from:" + from + ",to:" + to + ",departure:" + d1 + "TANYT";
        String url = "https://www.expedia.com/Flights-Search?trip=" + trip +
                "&leg1=" + enc(leg1) +
                "&passengers=adults:" + Math.max(1, adults) +
                "&options=cabinclass:" + mapCabinExpedia(cabin) +
                "&mode=search";
        if (d2 != null && !d2.isEmpty()) {
            String leg2 = "from:" + to + ",to:" + from + ",departure:" + d2 + "TANYT";
            url += "&leg2=" + enc(leg2);
        }
        return url;
    }
    private static String mapCabinExpedia(String cabin) {
        if (cabin == null) return "economy";
        cabin = cabin.toUpperCase();
        switch (cabin) {
            case "PREMIUM_ECONOMY": return "premium";
            case "BUSINESS": return "business";
            case "FIRST": return "first";
            default: return "economy";
        }
    }

    // Trip.com: /flights/LHR-DXB/tickets?dcity=LHR&acity=DXB&ddate=2025-11-21&rdate=...&adult=1&cabin=economy&flighttype=ow/rt
    private static String buildTripComLink(String from, String to, String d1, String d2, int adults, String cabin) {
        String base = "https://www.trip.com/flights/" + from + "-" + to + "/tickets";
        StringBuilder sb = new StringBuilder(base);
        sb.append("?dcity=").append(from)
                .append("&acity=").append(to)
                .append("&ddate=").append(d1)
                .append("&adult=").append(Math.max(1, adults))
                .append("&cabin=").append((cabin == null) ? "economy" : cabin.toLowerCase());
        if (d2 != null && !d2.isEmpty()) {
            sb.append("&rdate=").append(d2).append("&flighttype=rt");
        } else {
            sb.append("&flighttype=ow");
        }
        return sb.toString();
    }
}