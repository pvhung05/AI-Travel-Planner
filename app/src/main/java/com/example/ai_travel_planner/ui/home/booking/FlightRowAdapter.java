package com.example.ai_travel_planner.ui.home.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ai_travel_planner.R;
import com.bumptech.glide.Glide;
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

        String code = r.airline == null ? "" : r.airline.trim().toUpperCase();
        String logo = code.isEmpty() ? null : ("https://pics.avs.io/80/80/" + code + ".png");
        if (logo != null) {
            Glide.with(h.itemView.getContext())
                    .load(logo)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(h.imgLogo);
        } else {
            h.imgLogo.setImageResource(R.mipmap.ic_launcher);
        }

        h.txtAirline.setText(code + "  " + (r.flightNo == null ? "" : r.flightNo));
        h.txtRoute.setText(r.from + " → " + r.to);
        h.txtTime.setText(formatTime(r.depTime) + " → " + formatTime(r.arrTime));

        String meta = (r.duration == null ? "" : r.duration) +
                ((r.stops > 0) ? (" • " + r.stops + " stop" + (r.stops>1?"s":"")) : " • Nonstop");
        h.txtMeta.setText(meta);
        String priceText = (r.currency == null || r.currency.isEmpty())
                ? String.format("%,.0f", r.price)
                : String.format("%,.0f %s", r.price, r.currency);
        h.txtPrice.setText(priceText);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(r); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView txtAirline, txtRoute, txtTime, txtMeta, txtPrice;
        VH(@NonNull View v) {
            super(v);
            imgLogo   = v.findViewById(R.id.imgLogo);
            txtAirline = v.findViewById(R.id.txtAirline);
            txtRoute   = v.findViewById(R.id.txtRoute);
            txtTime    = v.findViewById(R.id.txtTime);
            txtMeta    = v.findViewById(R.id.txtMeta);
            txtPrice   = v.findViewById(R.id.txtPrice);
        }
    }

    private static String formatTime(String iso) {
        if (iso == null) return "";
        String s = iso.trim();
        int t = s.indexOf('T');
        if (t >= 0 && s.length() >= t+6) return s.substring(t+1, t+6);
        int sp = s.indexOf(' ');
        if (sp >= 0 && s.length() >= sp+6) return s.substring(sp+1, sp+6);
        if (s.length() >= 5) return s.substring(s.length()-5);
        return s;
    }
}