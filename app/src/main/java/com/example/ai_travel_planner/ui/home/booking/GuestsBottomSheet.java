package com.example.ai_travel_planner.ui.home.booking;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ai_travel_planner.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class GuestsBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onDone(int rooms, int adults, int children);
    }

    private Listener listener;

    public GuestsBottomSheet(Listener l) {
        this.listener = l;
    }

    private NumberPicker npRooms, npAdults, npChildren;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bs_guests, container, false);
        npRooms = v.findViewById(R.id.npRooms);
        npAdults = v.findViewById(R.id.npAdults);
        npChildren = v.findViewById(R.id.npChildren);

        config(npRooms, 1, 9, 1);
        config(npAdults, 1, 9, 2);
        config(npChildren, 0, 9, 0);

        v.findViewById(R.id.btnDone).setOnClickListener(view -> {
            if (listener != null) {
                listener.onDone(npRooms.getValue(), npAdults.getValue(), npChildren.getValue());
            }
            dismiss();
        });
        return v;
    }

    private void config(NumberPicker np, int min, int max, int def) {
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setValue(def);
        np.setWrapSelectorWheel(true);
    }
}