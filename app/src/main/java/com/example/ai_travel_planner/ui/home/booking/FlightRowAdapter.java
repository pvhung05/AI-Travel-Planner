package com.example.ai_travel_planner.ui.home.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.booking.FlightResultsActivity.FlightRow;

import java.util.List;

public class FlightRowAdapter extends RecyclerView.Adapter<FlightRowAdapter.VH> {

    private final List<FlightRow> items;

    public FlightRowAdapter(List<FlightRow> items) { this.items = items; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight_offer, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        FlightRow r = items.get(pos);
        h.txtAirline.setText(r.airline + "  " + r.flightNo);
        h.txtRoute.setText(r.from + " → " + r.to);
        h.txtTime.setText(r.depTime + " → " + r.arrTime);
        h.txtPrice.setText(String.format("%,.0f %s", r.price, r.currency));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtAirline, txtRoute, txtTime, txtPrice;
        VH(@NonNull View v) {
            super(v);
            txtAirline = v.findViewById(R.id.txtAirline);
            txtRoute   = v.findViewById(R.id.txtRoute);
            txtTime    = v.findViewById(R.id.txtTime);
            txtPrice   = v.findViewById(R.id.txtPrice);
        }
    }
}