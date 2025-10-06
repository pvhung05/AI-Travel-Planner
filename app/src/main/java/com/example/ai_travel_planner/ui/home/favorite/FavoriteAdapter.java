package com.example.ai_travel_planner.ui.home.favorite;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.describe.DescribeScreen;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<FavoriteItem> favoriteList;

    public FavoriteAdapter(List<FavoriteItem> favoriteList) {
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_item, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteItem item = favoriteList.get(position);
        holder.tvPlaceName.setText(item.getName());
        holder.tvAddress.setText(item.getAddress());
        holder.tvRating.setText(String.valueOf(item.getRating()));
        holder.ivPlace.setImageResource(item.getImageRes());

        holder.ivPlace.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DescribeScreen.class);
            intent.putExtra("placeName", item.getName());
            intent.putExtra("source", "favorite"); // Thêm nguồn gốc
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlace, ivStar;
        TextView tvPlaceName, tvAddress, tvRating;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlace = itemView.findViewById(R.id.ivPlace);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivStar = itemView.findViewById(R.id.ivStar);
        }
    }
}