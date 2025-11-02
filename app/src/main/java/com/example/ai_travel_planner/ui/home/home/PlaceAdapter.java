// app/src/main/java/com/example/ai_travel_planner/ui/home/home/PlaceAdapter.java
package com.example.ai_travel_planner.ui.home.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log; // <-- THÊM IMPORT LOG
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ai_travel_planner.ui.home.describe.DescribeScreen;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.ui.home.plan.PlaceItem; // Đảm bảo import đúng

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<PlaceItem> placeItems;
    private Context context;

    public PlaceAdapter(Context context, List<PlaceItem> placeItems) {
        this.context = context;
        // Khởi tạo an toàn hơn, tránh null
        this.placeItems = placeItems != null ? placeItems : new ArrayList<>();
        Log.d("AdapterInit", "Adapter created with initial items: " + this.placeItems.size()); // Log khởi tạo
    }

    public void updateData(List<PlaceItem> newPlaceItems) {
        // Log kích thước list mới nhận được
        Log.d("AdapterUpdate", "Updating data. Received new list size: " + (newPlaceItems != null ? newPlaceItems.size() : 0));
        if (this.placeItems == null) {
            this.placeItems = new ArrayList<>();
        }
        this.placeItems.clear();
        if (newPlaceItems != null) {
            this.placeItems.addAll(newPlaceItems);
        }
        notifyDataSetChanged();
        // Log số lượng item sau khi cập nhật
        Log.d("AdapterUpdate", "notifyDataSetChanged called. Current getItemCount(): " + getItemCount());
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("AdapterLifecycle", "onCreateViewHolder called"); // Log khi tạo ViewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        // Log vị trí đang được bind
        Log.d("AdapterBind", "onBindViewHolder called for position: " + position);

        // Kiểm tra an toàn trước khi lấy item
        if (placeItems == null || position < 0 || position >= placeItems.size()) {
            Log.e("AdapterBind", "Invalid position or null list at position: " + position);
            return;
        }

        PlaceItem item = placeItems.get(position);
        if (item == null) {
            Log.e("AdapterBind", "PlaceItem is null at position: " + position);
            return; // Dừng lại nếu item null
        }

        // Log dữ liệu đang bind
        Log.d("AdapterBind", "Binding data: Name=" + item.getName() + ", Vicinity=" + item.getVicinity() + ", ImageUrl=" + item.getImageUrl());

        holder.tvPlaceName.setText(item.getName());
        // holder.tvPlaceVicinity.setText(item.getVicinity()); // Bỏ comment nếu có TextView này

        if (item.getRating() != null && item.getRating() > 0) {
            holder.tvPlaceRating.setText(String.format("%.1f", item.getRating())); // Format "4.8"
            holder.tvPlaceRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvPlaceRating.setVisibility(View.GONE); // Ẩn đi nếu không có rating
        }

        // Tải ảnh bằng Glide
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                Glide.with(holder.itemView.getContext()) // Dùng context của itemView an toàn hơn
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.placeholder_image) // Thay bằng ảnh chờ của bạn
                        .error(R.drawable.error_image)         // Thay bằng ảnh lỗi của bạn
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imgPlace);
                Log.d("AdapterGlide", "Loading image URL: " + item.getImageUrl());
            } catch (Exception e) {
                // Log lỗi Glide nếu có
                Log.e("AdapterGlide", "Glide error for URL " + item.getImageUrl(), e);
                holder.imgPlace.setImageResource(R.drawable.error_image); // Hiện ảnh lỗi
            }
        } else {
            Log.d("AdapterGlide", "Image URL is null or empty for: " + item.getName() + ". Setting default image.");
            holder.imgPlace.setImageResource(R.drawable.default_image); // Ảnh mặc định nếu không có URL
        }

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            Log.d("AdapterClick", "Item clicked: " + item.getName()); // Log khi click
            Intent intent = new Intent(context, DescribeScreen.class);
            intent.putExtra("placeName", item.getName());
            intent.putExtra("placeVicinity", item.getVicinity());
            intent.putExtra("placeImageUrl", item.getImageUrl());
            intent.putExtra("placeRating", item.getRating());
            intent.putExtra("PLACE_IMAGE_URL", item.getImageUrl());
            intent.putExtra("PLACE_DESCRIPTION", item.getDescription());
            intent.putExtra("PLACE_LATITUDE", item.getLatitude());
            intent.putExtra("PLACE_LONGITUDE", item.getLongitude());
            intent.putExtra("source", "home");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        int count = placeItems == null ? 0 : placeItems.size();
        // Log số lượng item sẽ trả về
        // Log.v("AdapterCount", "getItemCount() returning: " + count); // Dùng Log.v để tránh spam Logcat quá nhiều
        return count;
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceName;
        // TextView tvPlaceVicinity; // Thêm nếu có
        ImageView imgPlace;
        TextView tvPlaceRating;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            // tvPlaceVicinity = itemView.findViewById(R.id.tvPlaceVicinity); // Ánh xạ nếu có
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvPlaceRating = itemView.findViewById(R.id.tvPlaceRating);
        }
    }
}