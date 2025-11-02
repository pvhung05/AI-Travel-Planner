package com.example.ai_travel_planner.network.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model dùng để bind/parse JSON kế hoạch chuyến đi và truyền qua Intent (Serializable).
 * Đây KHÔNG phải Room entity.
 */
public class TripPlan implements Serializable {
    // id tuỳ chọn (nếu bạn muốn gắn với bản ghi trong Room)
    public String id;

    // Thông tin chung
    public String destination;   // ví dụ: "Hà Nội", "Đà Nẵng"
    public long   budgetVnd;     // ngân sách dự kiến (VND)
    public String style;         // phong cách: ẩm thực / thiên nhiên / văn hoá...
    public String aboutText;     // đoạn mô tả/giới thiệu (markdown/text) để show ở GuideScreen

    // Danh sách ngày
    public List<DayPlan> days = new ArrayList<>();

    // Tiện ích: số ngày
    public int getDaysCount() {
        return (days == null) ? 0 : days.size();
    }
}
