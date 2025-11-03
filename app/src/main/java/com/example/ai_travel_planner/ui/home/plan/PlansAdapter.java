package com.example.ai_travel_planner.ui.home.plan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.VH> {

    public interface OnPlanClick {
        void onClick(PlanEntity plan);
    }

    public interface OnSelectionChanged {
        void onSelectionChanged(boolean hasSelection);
    }

    private final List<PlanEntity> data = new ArrayList<>();
    private final OnPlanClick listener;
    private final Set<Long> selectedIds = new HashSet<>();
    private OnSelectionChanged selectionListener;

    public PlansAdapter(OnPlanClick l) {
        this.listener = l;
    }

    public void setData(List<PlanEntity> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionChanged(!selectedIds.isEmpty());
    }

    public void toggleSelection(long id) {
        if (selectedIds.contains(id)) selectedIds.remove(id); else selectedIds.add(id);
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionChanged(!selectedIds.isEmpty());
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionChanged(false);
    }

    public Set<Long> getSelectedIds() { return new HashSet<>(selectedIds); }

    public void setOnSelectionChanged(OnSelectionChanged l) { this.selectionListener = l; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PlanEntity p = data.get(position);
        h.tvTitle.setText(p.destination);

        String url = (p.imageUrl != null && !p.imageUrl.isEmpty())
                ? p.imageUrl
                : ImageResolver.buildFallbackUrl(p.destination);

        Glide.with(h.itemView.getContext())
                .load(url)
                .placeholder(R.drawable.sample_mountain)
                .centerCrop()
                .into(h.img);

        h.cb.setChecked(selectedIds.contains(p.id));
        h.cb.setOnClickListener(v -> toggleSelection(p.id));

        h.itemView.setOnClickListener(v -> {
            if (!selectedIds.isEmpty()) {
                toggleSelection(p.id);
            } else if (listener != null) {
                listener.onClick(p);
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle;
        CheckBox cb;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgThumb);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            cb = itemView.findViewById(R.id.cbSelect);
        }
    }
}