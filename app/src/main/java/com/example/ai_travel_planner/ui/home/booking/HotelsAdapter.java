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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelsAdapter extends RecyclerView.Adapter<HotelsAdapter.VH> {

    public interface OnHotelClick { void onClick(Hotel h); }

    private final List<Hotel> data = new ArrayList<>();
    private final OnHotelClick listener;

    // Formatter cho VNĐ: 1.200.000
    private final NumberFormat vndFormat =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    public HotelsAdapter(OnHotelClick l) {
        this.listener = l;
        vndFormat.setMaximumFractionDigits(0); // không lấy phần thập phân
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

        h.tvName.setText(nullToEmpty(it.name));
        h.tvAddr.setText(shortAddr(it.address));

        // Giá: 1.200.000 VNĐ hoặc "—" nếu không có
        if (it.priceMin > 0) {
            String priceText = vndFormat.format(it.priceMin) + " VNĐ";
            h.tvPrice.setText(priceText);
        } else {
            h.tvPrice.setText("—");
        }

        // Rating: ★ 8.7 hoặc "—"
        if (it.rating > 0f) {
            h.tvRating.setText(String.format(Locale.US, "★ %.1f", it.rating));
        } else {
            h.tvRating.setText("—");
        }

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

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static String shortAddr(String addr) {
        if (addr == null) return "";
        // Cắt gọn một chút cho đẹp UI
        return addr.length() > 80 ? addr.substring(0, 77) + "…" : addr;
    }

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