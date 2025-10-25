package com.example.ai_travel_planner.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plans")
public class PlanEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String destination;  // địa danh
    public int days;            // số ngày
    public String style;        // phong cách
    public double budget;       // ngân sách
    public String content;      // toàn bộ text AI trả về (đã sanitize)
    public long createdAt;      // thời gian lưu (millis)
    public String imageUrl;
    // Bạn có thể thêm trường imageUrl nếu muốn tải ảnh 1 lần và cache
    // public String imageUrl;
}