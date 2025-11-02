package com.example.ai_travel_planner.ui.home.itinerary;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ai_travel_planner.R; // <-- import R đúng package app
import com.example.ai_travel_planner.network.models.ActivityItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;
import java.util.UUID;

public class EditActivityBottomSheet extends BottomSheetDialogFragment {

    public interface OnSave { void onSave(ActivityItem item); }

    private static final String ARG_ITEM = "arg_item";

    private ActivityItem item;
    private OnSave onSave;

    // Gọi từ ItineraryScreen:
    // EditActivityBottomSheet.show(getSupportFragmentManager(), currentItem, result -> { ... });
    public static void show(androidx.fragment.app.FragmentManager fm,
                            @Nullable ActivityItem item,
                            OnSave cb) {
        EditActivityBottomSheet s = new EditActivityBottomSheet();
        Bundle b = new Bundle();
        if (item != null) b.putSerializable(ARG_ITEM, item); // OK vì đã implements Serializable
        s.setArguments(b);
        s.onSave = cb;
        s.show(fm, "EditActivityBottomSheet");
    }

    @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.bottomsheet_edit_activity, null, false);
        dialog.setContentView(v);

        // Lấy item từ args (nếu có)
        Bundle args = getArguments();
        if (args != null) {
            Object obj = args.getSerializable(ARG_ITEM);
            if (obj instanceof ActivityItem) item = (ActivityItem) obj;
        }

        TimePicker tp = v.findViewById(R.id.timePicker);
        tp.setIs24HourView(true); // set 24h bằng code

        EditText edTitle    = v.findViewById(R.id.edTitle);
        EditText edLocation = v.findViewById(R.id.edLocation);
        EditText edNotes    = v.findViewById(R.id.edNotes);
        Spinner  spCategory = v.findViewById(R.id.spCategory);
        Button   btnSave    = v.findViewById(R.id.btnSaveActivity);

        String[] cats = new String[]{"food","nature","culture","shopping","adventure","other"};
        spCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, cats));

        // Prefill nếu sửa
        if (item != null) {
            if (item.time != null) {
                String[] hm = item.time.split(":");
                int h = safeParse(hm, 0, 8), m = safeParse(hm, 1, 0);
                if (android.os.Build.VERSION.SDK_INT >= 23) { tp.setHour(h); tp.setMinute(m); }
                else { tp.setCurrentHour(h); tp.setCurrentMinute(m); }
            }
            edTitle.setText(item.title);
            edLocation.setText(item.location);
            edNotes.setText(item.notes);
            int idx = indexOf(cats, item.category);
            if (idx >= 0) spCategory.setSelection(idx);
        }

        btnSave.setOnClickListener(view -> {
            ActivityItem out = (item != null) ? item : new ActivityItem(); // cần no-args ctor
            if (out.id == null || out.id.isEmpty()) out.id = UUID.randomUUID().toString();

            int h, m;
            if (android.os.Build.VERSION.SDK_INT >= 23) { h = tp.getHour(); m = tp.getMinute(); }
            else { h = tp.getCurrentHour(); m = tp.getCurrentMinute(); }

            out.time = String.format(Locale.US, "%02d:%02d", h, m);
            out.title = edTitle.getText().toString().trim();
            out.location = edLocation.getText().toString().trim();
            out.notes = edNotes.getText().toString().trim();
            out.category = String.valueOf(spCategory.getSelectedItem());

            if (onSave != null) onSave.onSave(out);
            dismiss();
        });

        return dialog;
    }

    private static int indexOf(String[] arr, String v) {
        if (v == null) return -1;
        for (int i = 0; i < arr.length; i++) if (v.equalsIgnoreCase(arr[i])) return i;
        return -1;
    }

    private static int safeParse(String[] hm, int i, int def) {
        try { return Integer.parseInt(hm[i]); } catch (Exception e) { return def; }
    }
}


