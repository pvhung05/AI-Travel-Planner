package com.example.ai_travel_planner.ui.home.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.ui.home.describe.DescribeScreen;
import com.example.ai_travel_planner.R;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<String> places;

    public PlaceAdapter(List<String> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        String place = places.get(position);
        holder.tvPlaceName.setText(place);

        // Khi click ảnh thì mở DescribeScreen
        holder.imgPlace.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DescribeScreen.class);
            intent.putExtra("placeName", place);
            intent.putExtra("source", "home"); // Thêm nguồn gốc
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceName;
        ImageView imgPlace;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            imgPlace = itemView.findViewById(R.id.imgPlace);
        }
    }
}