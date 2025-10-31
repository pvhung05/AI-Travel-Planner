package com.example.ai_travel_planner.ui.home.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ai_travel_planner.R;
import java.util.List;

public class ProviderRowAdapter extends RecyclerView.Adapter<ProviderRowAdapter.VH> {

    public interface OnProviderClick { void onClick(FlightDetailActivity.ProviderLink p); }

    private final List<FlightDetailActivity.ProviderLink> items;
    private final OnProviderClick listener;

    public ProviderRowAdapter(List<FlightDetailActivity.ProviderLink> items, OnProviderClick l) {
        this.items = items;
        this.listener = l;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_provider_link, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        FlightDetailActivity.ProviderLink p = items.get(pos);
        h.tvName.setText(p.name);
        String priceText = (p.currency == null || p.currency.isEmpty())
                ? String.format("From %,.0f", p.priceFrom)
                : String.format("From %,.0f %s", p.priceFrom, p.currency);
        h.tvPrice.setText(priceText);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(p); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        VH(@NonNull View v) {
            super(v);
            tvName  = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
        }
    }
}