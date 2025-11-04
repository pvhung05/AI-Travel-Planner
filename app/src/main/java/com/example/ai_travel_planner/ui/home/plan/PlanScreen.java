// app/src/main/java/com/example/ai_travel_planner/ui/home/plan/PlanScreen.java
package com.example.ai_travel_planner.ui.home.plan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.dao.PlanDao;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.ui.home.login.SigninScreen;
import com.example.ai_travel_planner.ui.home.plan.GuideScreen;
import com.example.ai_travel_planner.utils.BottomNav;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class PlanScreen extends AppCompatActivity {

    private RecyclerView rvPlans;
    private TextView tvEmpty;
    private PlansAdapter adapter;
    private Button btnDeleteSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planscreen);

        rvPlans = findViewById(R.id.rvPlans);
        tvEmpty = findViewById(R.id.tvSubtitle); // dùng tạm subtitle làm empty hint
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        rvPlans.setLayoutManager(new GridLayoutManager(this, 1));

        adapter = new PlansAdapter(plan -> {
            // Mở trực tiếp ItineraryScreen với dữ liệu đã lưu
            Intent it = new Intent(PlanScreen.this, com.example.ai_travel_planner.ui.home.itinerary.ItineraryScreen.class);
            if (plan.planJson != null && !plan.planJson.isEmpty()) {
                it.putExtra("plan_text", plan.planJson);
            }
            it.putExtra("plan_id", plan.id); // Truyền plan_id để update đúng plan
            startActivity(it);
        });
        rvPlans.setAdapter(adapter);
        adapter.setOnSelectionChanged(has -> updateDeleteButton());
        btnDeleteSelected.setOnClickListener(v -> confirmAndDeleteSelected());
        updateDeleteButton();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNav.setupNavigation(this, bottomNav, R.id.nav_time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
    }

    private void loadPlans() {
        new Thread(() -> {
            List<PlanEntity> all = AppDatabase.getInstance(this).planDao().getAll();
            runOnUiThread(() -> {
                adapter.setData(all);
                boolean empty = (all == null || all.isEmpty());
                tvEmpty.setText(empty ? "Chưa có lịch trình nào. Hãy tạo bằng AI nhé!" : "Your saved trips");
                rvPlans.setVisibility(empty ? View.GONE : View.VISIBLE);
                updateDeleteButton();
            });
        }).start();
    }

    private void updateDeleteButton() {
        boolean enabled = adapter != null && !adapter.getSelectedIds().isEmpty();
        if (btnDeleteSelected != null) {
            btnDeleteSelected.setEnabled(enabled);
            if (enabled) {
                btnDeleteSelected.setBackgroundResource(R.drawable.bg_button_filled_red);
                btnDeleteSelected.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            } else {
                btnDeleteSelected.setBackgroundResource(R.drawable.bg_button_disabled);
                btnDeleteSelected.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }
        }
    }

    private void confirmAndDeleteSelected() {
        final java.util.Set<Long> ids = adapter.getSelectedIds();
        if (ids.isEmpty()) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xoá kế hoạch đã chọn?")
                .setMessage("Bạn chắc chắn muốn xoá " + ids.size() + " kế hoạch?")
                .setPositiveButton("Xoá", (d, w) -> deleteSelected(ids))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteSelected(java.util.Set<Long> ids) {
        new Thread(() -> {
            PlanDao dao = AppDatabase.getInstance(this).planDao();
            for (Long id : ids) {
                try { dao.deleteById(id); } catch (Exception ignore) {}
            }
            runOnUiThread(() -> {
                adapter.clearSelection();
                loadPlans();
            });
        }).start();
    }
}