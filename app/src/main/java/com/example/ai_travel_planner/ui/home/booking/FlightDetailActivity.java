package com.example.ai_travel_planner.ui.home.booking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.example.ai_travel_planner.R;

import java.util.Locale;

/**
 * Màn chi tiết một offer chuyến bay.
 * - Hiển thị thông tin đã chọn ở FlightResultsActivity
 * - 3 nút mở deeplink đã AUTOFILL:
 *     + Google Flights
 *     + Skyscanner (định dạng /transport/flights/{from}/{to}/{ddMMyy}/{ddMMyy}/?... )
 *     + Kiwi (search/results/{from}-{to}/{yyyy-MM-dd}/{yyyy-MM-dd}?adults=...)
 *
 *  Extras đầu vào (name → type):
 *  ------------------------------------------------------------
 *   "airline"   → String
 *   "flightNo"  → String
 *   "from"      → String (IATA city/airport code)
 *   "to"        → String (IATA city/airport code)
 *   "depTime"   → String (hiển thị)
 *   "arrTime"   → String (hiển thị)
 *   "price"     → double
 *   "currency"  → String
 *   "stops"     → int
 *   "duration"  → String
 *   "baggage"   → String
 *
 *  Các tham số tìm kiếm để build deeplink (có thể trùng với phần trên):
 *   "departISO" → String (yyyy-MM-dd)
 *   "returnISO" → String (yyyy-MM-dd or empty for one-way)
 *   "adults"    → int (>=1)
 *   "cabin"     → String (ECONOMY | PREMIUM_ECONOMY | BUSINESS | FIRST)
 */
public class FlightDetailActivity extends AppCompatActivity {

    // dữ liệu hiển thị
    private String airline, flightNo, fromIata, toIata, depTime, arrTime, currency, duration, baggage;
    private double price;
    private int stops;

    // dữ liệu build deeplink
    private String departISO, returnISO, cabin;
    private int adults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_detail);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvTitle    = findViewById(R.id.txtTitle);
        TextView tvRoute    = findViewById(R.id.txtRoute);
        TextView tvTime     = findViewById(R.id.txtTime);
        TextView tvStops    = findViewById(R.id.txtStops);
        TextView tvDuration = findViewById(R.id.txtDuration);
        TextView tvBaggage  = findViewById(R.id.txtBaggage);
        TextView tvPrice    = findViewById(R.id.txtPrice);

        Button btnGoogle    = findViewById(R.id.btnGoogleFlights);
        // --------- Đọc extras ---------
        Intent it = getIntent();
        airline   = it.getStringExtra("airline");
        flightNo  = it.getStringExtra("flightNo");
        fromIata  = it.getStringExtra("from");
        toIata    = it.getStringExtra("to");
        depTime   = it.getStringExtra("depTime");
        arrTime   = it.getStringExtra("arrTime");
        price     = it.getDoubleExtra("price", 0);
        currency  = it.getStringExtra("currency");
        stops     = it.getIntExtra("stops", 0);
        duration  = it.getStringExtra("duration");
        baggage   = it.getStringExtra("baggage");

        // tham số để build deeplink
        departISO = it.getStringExtra("departISO");
        returnISO = it.getStringExtra("returnISO");
        adults    = Math.max(1, it.getIntExtra("adults", 1));
        cabin     = it.getStringExtra("cabin");
        if (cabin == null || cabin.isEmpty()) cabin = "ECONOMY";

        // --------- Bind UI ---------
        tvTitle.setText(safe(airline) + (notEmpty(flightNo) ? ("  " + flightNo) : ""));
        tvRoute.setText(String.format(Locale.US, "%s → %s", safe(fromIata), safe(toIata)));
        tvTime.setText(String.format(Locale.US, "%s → %s", safe(depTime), safe(arrTime)));
        tvStops.setText(stops == 0 ? "Direct" : (stops + " stop" + (stops > 1 ? "s" : "")));
        tvDuration.setText(notEmpty(duration) ? duration : "");
        tvBaggage.setText(notEmpty(baggage) ? baggage : "");
        tvPrice.setText(formatPrice(price, currency));

        btnBack.setOnClickListener(v -> finish());

        // --------- Mở deeplinks ---------
        btnGoogle.setOnClickListener(v -> {
            String url = BookingDeepLinks.googleFlights(fromIata, toIata, departISO, returnISO, adults, cabin, currency);
            open(url);
        });


        // Có thể để Expedia mở Google như fallback (hoặc sau này thay bằng API chính thức)
    }

    // ===== Helpers =====

    private void open(String url) {
        try {
            CustomTabsIntent tabs = new CustomTabsIntent.Builder().build();
            tabs.launchUrl(this, Uri.parse(url));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception ex) {
                Toast.makeText(this, "Không mở được link: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String formatPrice(double price, String currency) {
        if (price <= 0) return "See provider for latest price";
        String cur = currency == null ? "" : currency;
        return String.format(Locale.US, "%,.0f %s", price, cur);
    }
}