package com.example.ai_travel_planner.ui.home.booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.R;

import java.util.Calendar;
import java.util.Locale;

public class PlaneBookingScreen extends AppCompatActivity {

    // Views trong layout plane_booking.xml (giữ nguyên id)
    private ImageButton btnBack;
    private LinearLayout tabPlane, tabBus;
    private TextView txtFrom, txtTo, txtDate, pillTripType, txtPassengers;

    // State
    private boolean roundTrip = true;
    private boolean pickFrom = true;     // true: đang chọn From; false: To
    private int adults = 1;

    private String fromIata = "HAN";
    private String toIata   = "SGN";
    private String departISO = plusDaysISO(14);
    private String returnISO = plusDaysISO(21);

    // Mở màn hình chọn sân bay (autocomplete)
    private final ActivityResultLauncher<Intent> pickAirport =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                    String iata  = r.getData().getStringExtra(AirportSearchActivity.EXTRA_IATA);
                    String label = r.getData().getStringExtra(AirportSearchActivity.EXTRA_LABEL);
                    if (iata != null) {
                        if (pickFrom) {
                            fromIata = iata;
                            txtFrom.setText(label != null ? label : iata);
                        } else {
                            toIata = iata;
                            txtTo.setText(label != null ? label : iata);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plane_booking);

        // Map views
        btnBack       = findViewById(R.id.btnBack);
        tabPlane      = findViewById(R.id.tabPlane);
        tabBus        = findViewById(R.id.tabBus);
        txtFrom       = findViewById(R.id.txtFrom);
        txtTo         = findViewById(R.id.txtTo);
        txtDate       = findViewById(R.id.txtDate);
        pillTripType  = findViewById(R.id.pillTripType);
        txtPassengers = findViewById(R.id.txtPassengers);

        // Initial render
        txtFrom.setText(fromIata);
        txtTo.setText(toIata);
        render();

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Tabs (hiện chỉ dùng Plane, Bus để sau)
        tabPlane.setOnClickListener(v -> selectPlane());
        tabBus.setOnClickListener(v -> selectBus()); // placeholder

        // Chọn From / To
        View.OnClickListener pickFromListener = v -> {
            pickFrom = true;
            pickAirport.launch(new Intent(this, AirportSearchActivity.class));
        };
        View.OnClickListener pickToListener = v -> {
            pickFrom = false;
            pickAirport.launch(new Intent(this, AirportSearchActivity.class));
        };
        txtFrom.setOnClickListener(pickFromListener);
        txtTo.setOnClickListener(pickToListener);

        // Toggle One way / Round trip
        pillTripType.setOnClickListener(v -> {
            roundTrip = !roundTrip;
            if (!roundTrip) returnISO = "";
            render();
        });

        // Chọn ngày
        txtDate.setOnClickListener(v -> openDatePickers());

        // Chọn số khách (đơn giản: chạm để +1 vòng 1..9)
        txtPassengers.setOnClickListener(v -> {
            adults = Math.max(1, (adults % 9) + 1);
            render();
        });

        // SEARCH
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent it = new Intent(this, FlightResultsActivity.class);
            it.putExtra("from", fromIata);
            it.putExtra("to", toIata);
            it.putExtra("depart", departISO);
            it.putExtra("ret", returnISO);
            it.putExtra("adults", adults);
            startActivity(it);
        });
    }

    private void render() {
        pillTripType.setText(roundTrip ? "Round Trip" : "One Way");
        txtPassengers.setText(adults + " Adult" + (adults > 1 ? "s" : ""));
        String dateText = departISO + (roundTrip && !returnISO.isEmpty() ? " → " + returnISO : "");
        txtDate.setText(dateText);
    }

    private void openDatePickers() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    departISO = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                    if (roundTrip) {
                        DatePickerDialog dp2 = new DatePickerDialog(
                                this,
                                (v2, y2, m2, d2) -> {
                                    returnISO = String.format(Locale.US, "%04d-%02d-%02d", y2, m2 + 1, d2);
                                    render();
                                },
                                y, m, d
                        );
                        dp2.setTitle("Select return date");
                        dp2.show();
                    } else {
                        returnISO = "";
                        render();
                    }
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        dp.setTitle("Select departure date");
        dp.show();
    }

    private void selectPlane() {
        // Làm hiệu ứng chọn tab nếu bạn có selector; hiện giữ nguyên
        tabPlane.setAlpha(1f);
        tabBus.setAlpha(0.6f);
    }

    private void selectBus() {
        // Placeholder – tương lai mở BusBookingScreen
        tabPlane.setAlpha(0.6f);
        tabBus.setAlpha(1f);
    }

    // Utility
    private static String plusDaysISO(int d) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, d);
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }
}