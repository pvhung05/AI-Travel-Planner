package com.example.ai_travel_planner.ui.home.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;

import java.util.List;
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<ActivityItem> activities;

    public ActivityAdapter(List<ActivityItem> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = activities.get(position);
        holder.txtTime.setText(item.getTime());
        holder.txtTitle.setText(item.getTitle());
        holder.imgIcon.setImageResource(item.getIconRes());
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime, txtTitle;
        ImageView imgIcon;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}
