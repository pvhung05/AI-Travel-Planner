package com.example.ai_travel_planner.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ai_travel_planner.data.local.dao.PlanDao;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;

@Database(
        entities = { PlanEntity.class },
        version = 8,                // ↑ TĂNG VERSION để Room reset schema
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PlanDao planDao();

    // Giữ tên hàm đúng với PlanScreen đang gọi: getInstance(...)
    public static @NonNull AppDatabase getInstance(@NonNull Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    AppDatabase.class,
                                    "travel_planner.db"
                            )
                            // Dev: version thay đổi mà không có migration -> xóa DB cũ, tạo mới
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // (Tuỳ chọn) alias nếu chỗ khác gọi AppDatabase.get(...)
    public static @NonNull AppDatabase get(@NonNull Context ctx) {
        return getInstance(ctx);
    }
}
