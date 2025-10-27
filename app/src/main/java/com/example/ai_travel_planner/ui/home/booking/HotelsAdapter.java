package com.example.ai_travel_planner.ui.home.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.booking.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class HotelsAdapter extends RecyclerView.Adapter<HotelsAdapter.VH> {

    public interface OnHotelClick { void onClick(Hotel h); }

    private final List<Hotel> data = new ArrayList<>();
    private final OnHotelClick listener;

    public HotelsAdapter(OnHotelClick l) {
        this.listener = l;
    }

    public void setData(List<Hotel> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Hotel it = data.get(position);
        h.tvName.setText(it.name);
        h.tvAddr.setText(it.address == null ? "" : it.address);
        h.tvPrice.setText(it.priceMin > 0 ? ("~ " + it.priceMin + " USD") : "—");
        h.tvRating.setText(it.rating > 0 ? String.format("%.1f", it.rating) : "—");

        Glide.with(h.itemView.getContext())
                .load(it.thumbnailUrl)
                .placeholder(R.drawable.sample_mountain)
                .centerCrop()
                .into(h.img);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(it);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvAddr, tvPrice, tvRating;
        VH(@NonNull View v) {
            super(v);
            img     = v.findViewById(R.id.imgThumb);
            tvName  = v.findViewById(R.id.tvName);
            tvAddr  = v.findViewById(R.id.tvAddr);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvRating= v.findViewById(R.id.tvRating);
        }
    }
}