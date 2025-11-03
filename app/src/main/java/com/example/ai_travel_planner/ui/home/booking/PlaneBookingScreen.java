package com.example.ai_travel_planner.ui.home.booking;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_travel_planner.R;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PlaneBookingScreen extends AppCompatActivity {

    // ==== SharedPreferences keys ====
    private static final String PREFS       = "flight_booking_draft";
    private static final String K_FROM_IATA = "fromIata";
    private static final String K_FROM_LBL  = "fromLabel";
    private static final String K_TO_IATA   = "toIata";
    private static final String K_TO_LBL    = "toLabel";
    private static final String K_DEPART    = "departISO";
    private static final String K_RETURN    = "returnISO";
    private static final String K_ADULTS    = "adults";
    private static final String K_ROUND     = "roundTrip";
    private static final String K_CABIN     = "cabin";

    // ==== Views ====
    private ImageButton btnBack;
    private LinearLayout tabPlane, tabBus;
    private TextView txtFrom, txtTo, txtDate, txtPassengers, pillTripType;
    private Button btnSearch;

    // ==== State ====
    private boolean roundTrip = true;
    private String fromIata = "HAN";
    private String fromLabel = "Hanoi - HAN";
    private String toIata   = "SGN";
    private String toLabel  = "Ho Chi Minh City - SGN";
    private String departISO = ""; // yyyy-MM-dd (UTC)
    private String returnISO = "";
    private int adults = 1;
    private String cabin = "ECONOMY";

    private boolean pendingPickIsFrom = false;
    private SharedPreferences prefs;

    // Nhận kết quả chọn sân bay (DÙNG ĐÚNG KEY từ AirportSearchActivity)
    private final ActivityResultLauncher<Intent> pickAirport =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                    String iata  = r.getData().getStringExtra(AirportSearchActivity.EXTRA_IATA);
                    String label = r.getData().getStringExtra(AirportSearchActivity.EXTRA_LABEL);
                    if (iata == null) iata = "";
                    if (label == null) label = iata;

                    if (pendingPickIsFrom) {
                        fromIata  = iata;
                        fromLabel = label;
                        txtFrom.setText(label);
                    } else {
                        toIata  = iata;
                        toLabel = label;
                        txtTo.setText(label);
                    }
                    saveDraft();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plane_booking);

        // Bind views
        btnBack       = findViewById(R.id.btnBack);
        tabPlane      = findViewById(R.id.tabPlane);
        tabBus        = findViewById(R.id.tabBus);
        txtFrom       = findViewById(R.id.txtFrom);
        txtTo         = findViewById(R.id.txtTo);
        txtDate       = findViewById(R.id.txtDate);
        txtPassengers = findViewById(R.id.txtPassengers);
        pillTripType  = findViewById(R.id.pillTripType);
        btnSearch     = findViewById(R.id.btnSearch);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        restoreDraft();
        renderUI();

        btnBack.setOnClickListener(v -> finish());

        txtFrom.setOnClickListener(v -> openAirportPicker(true));
        txtTo.setOnClickListener(v -> openAirportPicker(false));

        pillTripType.setOnClickListener(v -> {
            roundTrip = !roundTrip;
            if (!roundTrip) returnISO = "";
            renderUI();
            saveDraft();
        });

        txtPassengers.setOnClickListener(v -> openPassengersDialog());

        txtDate.setOnClickListener(v -> openDatePicker());

        btnSearch.setOnClickListener(v -> goResults());
    }

    private void openAirportPicker(boolean isFrom) {
        pendingPickIsFrom = isFrom;
        Intent it = new Intent(this, AirportSearchActivity.class);
        it.putExtra("EXTRA_QUERY_HINT", isFrom ? fromLabel : toLabel);
        pickAirport.launch(it);
    }

    private void openPassengersDialog() {
        final String[] arr = {"1","2","3","4","5","6","7","8","9"};
        new AlertDialog.Builder(this)
                .setTitle("Adults")
                .setSingleChoiceItems(arr, Math.max(0, adults - 1), (dialog, which) -> adults = which + 1)
                .setPositiveButton("Xong", (d,w) -> {
                    txtPassengers.setText(adults + " Adult" + (adults>1?"s":""));
                    saveDraft();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        if (roundTrip) {
            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> b =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Select dates")
                            .setCalendarConstraints(constraints);

            Long s = parseISOToUtcMillis(departISO);
            Long e = parseISOToUtcMillis(returnISO);
            if (s != null && e != null && e >= s) {
                b.setSelection(new androidx.core.util.Pair<>(s, e));
            }
            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = b.build();
            picker.addOnPositiveButtonClickListener(sel -> {
                if (sel == null) return;
                departISO = toISOFromPickerUTC(sel.first);
                returnISO = toISOFromPickerUTC(sel.second);
                renderUI();
                saveDraft();
            });
            picker.show(getSupportFragmentManager(), "date_range");
        } else {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setCalendarConstraints(constraints)
                    .build();
            picker.addOnPositiveButtonClickListener(ms -> {
                departISO = toISOFromPickerUTC(ms);
                returnISO = "";
                renderUI();
                saveDraft();
            });
            picker.show(getSupportFragmentManager(), "date_single");
        }
    }

    private void goResults() {
        if (isEmpty(fromIata)) { toast("Vui lòng chọn điểm đi"); return; }
        if (isEmpty(toIata))   { toast("Vui lòng chọn điểm đến"); return; }

        // Nếu chưa chọn ngày, tự set mặc định để không bị chặn
        if (isEmpty(departISO)) {
            departISO = todayPlusDaysISO(7);
        }
        if (roundTrip && isEmpty(returnISO)) {
            returnISO = plusDaysISO(departISO, 3);
        }

        Intent it = new Intent(this, FlightResultsActivity.class);
        it.putExtra("from", fromIata);
        it.putExtra("to", toIata);
        it.putExtra("fromLabel", fromLabel);
        it.putExtra("toLabel", toLabel);
        it.putExtra("departISO", departISO);
        it.putExtra("returnISO", returnISO);
        it.putExtra("adults", adults);
        it.putExtra("cabin", cabin);
        it.putExtra("roundTrip", roundTrip);
        it.putExtra("currency", "VND");
        startActivity(it);
        saveDraft();
    }

    // ===== Render ====
    private void renderUI() {
        txtFrom.setText(isEmpty(fromLabel) ? labelFromIata(fromIata) : fromLabel);
        txtTo.setText(isEmpty(toLabel) ? labelFromIata(toIata) : toLabel);
        txtPassengers.setText(adults + " Adult" + (adults>1?"s":""));
        pillTripType.setText(roundTrip ? "Round Trip" : "One Way");

        if (isEmpty(departISO) && isEmpty(returnISO)) {
            txtDate.setText("Select date");
        } else if (!roundTrip) {
            txtDate.setText(prettyLocal(departISO));
        } else if (!isEmpty(departISO) && !isEmpty(returnISO)) {
            txtDate.setText(prettyLocal(departISO) + " → " + prettyLocal(returnISO));
        } else {
            txtDate.setText("Select dates");
        }
    }

    private static String labelFromIata(String code) {
        return isEmpty(code) ? "Select" : code;
    }

    // ===== Draft =====
    private void saveDraft() {
        prefs.edit()
                .putString(K_FROM_IATA, fromIata)
                .putString(K_FROM_LBL,  fromLabel)
                .putString(K_TO_IATA,   toIata)
                .putString(K_TO_LBL,    toLabel)
                .putString(K_DEPART,    departISO)
                .putString(K_RETURN,    returnISO)
                .putInt(K_ADULTS,       adults)
                .putBoolean(K_ROUND,    roundTrip)
                .putString(K_CABIN,     cabin)
                .apply();
    }

    private void restoreDraft() {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        fromIata  = prefs.getString(K_FROM_IATA, fromIata);
        fromLabel = prefs.getString(K_FROM_LBL,  fromLabel);
        toIata    = prefs.getString(K_TO_IATA,   toIata);
        toLabel   = prefs.getString(K_TO_LBL,    toLabel);
        departISO = prefs.getString(K_DEPART,    "");
        returnISO = prefs.getString(K_RETURN,    "");
        adults    = Math.max(1, prefs.getInt(K_ADULTS, 1));
        roundTrip = prefs.getBoolean(K_ROUND,    true);
        cabin     = prefs.getString(K_CABIN,     "ECONOMY");
    }

    // ===== Time helpers =====
    private static String toISOFromPickerUTC(long msUtcMidnight) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(msUtcMidnight));
    }

    private static Long parseISOToUtcMillis(String iso) {
        try {
            if (isEmpty(iso)) return null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(iso);
            return d != null ? d.getTime() : null;
        } catch (Exception e) { return null; }
    }

    private static String prettyLocal(String iso) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = in.parse(iso);
            SimpleDateFormat out = new SimpleDateFormat("EEE, dd/MM/yyyy", new Locale("vi"));
            out.setTimeZone(TimeZone.getDefault());
            return d != null ? out.format(d) : iso;
        } catch (Exception e) { return iso; }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String todayPlusDaysISO(int days) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
    }

    private static String plusDaysISO(String baseIso, int days) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(baseIso);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
            cal.setTime(d);
            cal.add(Calendar.DAY_OF_MONTH, days);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return baseIso;
        }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}