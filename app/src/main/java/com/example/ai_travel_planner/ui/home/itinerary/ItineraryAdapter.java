package com.example.ai_travel_planner.ui.home.itinerary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.models.AiPlanResponse;

import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.VH> {

    private final List<AiPlanResponse.DayPlan> data;

    public ItineraryAdapter(List<AiPlanResponse.DayPlan> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_plan, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AiPlanResponse.DayPlan d = data.get(pos);
        h.tvDay.setText("Day " + d.day);
        h.tvPlaces.setText("Places: " + (d.places==null? "-": String.join(", ", d.places)));
        h.tvFoods.setText("Foods: " + (d.foods==null? "-": String.join(", ", d.foods)));
        h.tvTrans.setText("Move: " + (d.transportHint==null? "-": d.transportHint));
        h.tvCost.setText("~ " + (long)d.estCost);
        h.tvNotes.setText(d.notes==null? "": d.notes);
    }

    @Override public int getItemCount() { return data==null?0:data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDay, tvPlaces, tvFoods, tvTrans, tvCost, tvNotes;
        VH(View v){
            super(v);
            tvDay = v.findViewById(R.id.tvDay);
            tvPlaces = v.findViewById(R.id.tvPlaces);
            tvFoods = v.findViewById(R.id.tvFoods);
            tvTrans = v.findViewById(R.id.tvTrans);
            tvCost = v.findViewById(R.id.tvCost);
            tvNotes = v.findViewById(R.id.tvNotes);
        }
    }
}