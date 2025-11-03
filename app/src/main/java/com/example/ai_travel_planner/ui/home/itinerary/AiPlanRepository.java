package com.example.ai_travel_planner.ui.home.itinerary;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.dao.PlanDao;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.network.models.TripPlan;
import com.google.gson.Gson;
import com.example.ai_travel_planner.ui.home.plan.ImageResolver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiPlanRepository {

    private static volatile AiPlanRepository INSTANCE;
    public static AiPlanRepository get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AiPlanRepository.class) {
                if (INSTANCE == null) INSTANCE = new AiPlanRepository(ctx.getApplicationContext());
            }
        }
        return INSTANCE;
    }
    // giữ tương thích code cũ
    public static AiPlanRepository getInstance(Context ctx) { return get(ctx); }

    private final PlanDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    private AiPlanRepository(Context appCtx) {
        this.dao = AppDatabase.getInstance(appCtx).planDao();   // <-- khởi tạo PlanDao ở đây
    }

    // ==== API bất đồng bộ (trả về main thread)
    public interface SaveCallback { void onSaved(long id); void onError(Exception e); }
    public interface LoadCallback { void onLoaded(TripPlan plan); }

    public void savePlan(TripPlan plan, SaveCallback cb) {
        io.execute(() -> {
            try {
                String dest = ns((plan.destination != null && !plan.destination.isEmpty()) ? plan.destination : plan.city);
                String style = ns(plan.style);
                int days = (plan == null) ? 0 : plan.getDaysCount();
                long now = System.currentTimeMillis();
                String json = gson.toJson(plan);

                // Try to find existing latest by meta and update instead of inserting a new row
                PlanEntity existing = dao.getLatestByMeta(dest, days, style);
                long id;
                if (existing != null) {
                    dao.updatePlanJson(existing.id, json, now);
                    if (existing.imageUrl == null || existing.imageUrl.isEmpty()) {
                        String fallback = (dest != null && !dest.isEmpty()) ? ImageResolver.buildFallbackUrl(dest) : null;
                        if (fallback != null) dao.updateImage(existing.id, fallback);
                    }
                    id = existing.id;
                } else {
                    PlanEntity e = new PlanEntity();
                    e.destination = dest;
                    e.style = style;
                    e.days = days;
                    e.createdAt = now;
                    e.planJson = json;
                    // set a displayable image so PlanScreen has thumbnail immediately
                    if (dest != null && !dest.isEmpty()) {
                        e.imageUrl = ImageResolver.buildFallbackUrl(dest);
                    }
                    id = dao.insert(e);
                }
                long finalId = id;
                main.post(() -> cb.onSaved(finalId));
            } catch (Exception ex) {
                main.post(() -> cb.onError(ex));
            }
        });
    }

    public void getLatestPlan(LoadCallback cb) {
        io.execute(() -> {
            PlanEntity e = dao.getLatest();
            TripPlan p = (e == null) ? null : gson.fromJson(e.planJson, TripPlan.class);
            main.post(() -> cb.onLoaded(p));
        });
    }

    public void getLatestPlanByMeta(String dest, int days, String style, LoadCallback cb) {
        io.execute(() -> {
            PlanEntity e = dao.getLatestByMeta(ns(dest), days, ns(style));
            TripPlan p = (e == null) ? null : gson.fromJson(e.planJson, TripPlan.class);
            main.post(() -> cb.onLoaded(p));
        });
    }

    public interface DeleteCallback { void onDone(); void onError(Exception e); }

    public void deleteById(long id, DeleteCallback cb) {
        io.execute(() -> {
            try {
                // Trường hợp DAO của bạn là:
                // @Query("DELETE FROM plans WHERE id = :id") void deleteById(long id);
                dao.deleteById(id);

                // Nếu DAO bạn dùng @Delete(PlanEntity):
                // PlanEntity e = dao.getById(id);
                // if (e != null) dao.delete(e);

                main.post(cb::onDone);
            } catch (Exception ex) {
                main.post(() -> cb.onError(ex));
            }
        });
    }

    // ==== API đồng bộ (nếu bạn tự chạy trong Thread riêng)
    public PlanEntity getPlanByIdSync(long id) { return dao.getById(id); }
    public PlanEntity getLatestPlanSync()     { return dao.getLatest();  }

    private static String ns(String s) { return s == null ? "" : s; }
}
