package com.example.ai_travel_planner.ui.home.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ai_travel_planner.R;
import java.util.List;

public class FlightRowAdapter extends RecyclerView.Adapter<FlightRowAdapter.VH> {

    public interface OnFlightClick { void onClick(FlightResultsActivity.FlightRow row); }

    private final List<FlightResultsActivity.FlightRow> items;
    private final OnFlightClick listener;

    public FlightRowAdapter(List<FlightResultsActivity.FlightRow> items, OnFlightClick l) {
        this.items = items;
        this.listener = l;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight_offer, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        FlightResultsActivity.FlightRow r = items.get(pos);
        h.txtAirline.setText(r.airline + "  " + r.flightNo);
        h.txtRoute.setText(r.from + " → " + r.to);
        h.txtTime.setText(r.depTime + " → " + r.arrTime);
        String priceText = (r.currency == null || r.currency.isEmpty())
                ? String.format("%,.0f", r.price)
                : String.format("%,.0f %s", r.price, r.currency);
        h.txtPrice.setText(priceText);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(r); });
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