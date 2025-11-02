package com.example.ai_travel_planner.ui.home.itinerary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.models.ActivityItem;

import java.util.HashSet;
import java.util.Set;

public class ItineraryAdapter extends ListAdapter<ActivityItem, ItineraryAdapter.VH> {

    public interface Callbacks {
        void onItemClick(ActivityItem item);                // mở editor
        void onSelectionChanged(Set<String> selectedIds);   // enable nút delete
    }

    private final Callbacks callbacks;
    private final Set<String> selectedIds = new HashSet<>();

    public ItineraryAdapter(Callbacks cb) {
        super(DIFF);
        this.callbacks = cb;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ActivityItem it = getItem(pos);
        h.txtTime.setText(it.time == null ? "--:--" : it.time);
        h.txtTitle.setText(it.title == null ? "" : it.title);

        // checkbox
        h.cb.setOnCheckedChangeListener(null);
        boolean checked = selectedIds.contains(it.id);
        h.cb.setChecked(checked);
        h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedIds.add(it.id);
            else selectedIds.remove(it.id);
            if (callbacks != null) callbacks.onSelectionChanged(new HashSet<>(selectedIds));
        });

        // click cả dòng để mở editor (không thay đổi checkbox)
        h.itemView.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onItemClick(it);
        });

        // icon theo category (food/nature/culture/shopping/adventure/other)
        int iconRes = mapIconByCategory(it.category, it.title);
        if (iconRes != 0) h.img.setImageResource(iconRes);
        else h.img.setImageDrawable(null);
    }

    public Set<String> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public void clearSelection() {
        selectedIds.clear();
        if (callbacks != null) callbacks.onSelectionChanged(new HashSet<>(selectedIds));
        notifyDataSetChanged();
    }

    private static int mapIconByCategory(String category, String title) {
        if (category == null) category = "";
        String c = category.toLowerCase();
        if (c.contains("food") || c.contains("eat") || c.contains("ẩm") || c.contains("ăn"))
            return R.drawable.ic_food;         // thêm drawable này
        if (c.contains("nature") || c.contains("park") || c.contains("thiên nhiên"))
            return R.drawable.ic_nature;       // thêm drawable này
        if (c.contains("culture") || c.contains("museum") || c.contains("văn hóa"))
            return R.drawable.ic_culture;      // thêm drawable này
        if (c.contains("shopping") || c.contains("shop"))
            return R.drawable.ic_shopping;     // thêm drawable này
        if (c.contains("adventure") || c.contains("ski") || c.contains("board") || c.contains("dive") || c.contains("sky"))
            return R.drawable.ic_adventure;    // thêm drawable này
        return R.drawable.ic_other;            // fallback
    }

    static final DiffUtil.ItemCallback<ActivityItem> DIFF = new DiffUtil.ItemCallback<ActivityItem>() {
        @Override public boolean areItemsTheSame(@NonNull ActivityItem a, @NonNull ActivityItem b) {
            return a.id != null && a.id.equals(b.id);
        }
        @Override public boolean areContentsTheSame(@NonNull ActivityItem a, @NonNull ActivityItem b) {
            return safe(a.time).equals(safe(b.time))
                    && safe(a.title).equals(safe(b.title))
                    && safe(a.category).equals(safe(b.category));
        }
        private String safe(String s) { return s == null ? "" : s; }
    };

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb; TextView txtTime; TextView txtTitle; ImageView img;
        VH(@NonNull View v) {
            super(v);
            cb = v.findViewById(R.id.cbSelect);
            txtTime = v.findViewById(R.id.txtTime);
            txtTitle = v.findViewById(R.id.txtTitle);
            img = v.findViewById(R.id.imgIcon);
        }
    }
}
