// app/src/main/java/com/example/ai_travel_planner/ui/home/booking/PlacesAdapter.java
package com.example.ai_travel_planner.ui.home.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.booking.net.GeoapifyResponse;

import java.util.ArrayList;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.VH> {

    public interface OnClick { void onClick(GeoapifyResponse.Properties p); }

    private final List<GeoapifyResponse.Properties> data = new ArrayList<>();
    private final OnClick listener;

    public PlacesAdapter(OnClick l){ this.listener = l; }

    public void setData(List<GeoapifyResponse.Properties> items){
        data.clear();
        if (items != null) {
            for (GeoapifyResponse.Properties p : items) if (p != null) data.add(p);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_suggestion, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        GeoapifyResponse.Properties p = data.get(pos);
        h.tvTitle.setText(p.name != null ? p.name : p.formatted);
        String sub = (p.city != null ? p.city + ", " : "") + (p.country != null ? p.country : "");
        h.tvSub.setText(sub);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(p); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvSub;
        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvSub   = v.findViewById(R.id.tvSub);
        }
    }
}