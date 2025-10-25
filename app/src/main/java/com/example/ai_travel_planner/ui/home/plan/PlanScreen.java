// app/src/main/java/com/example/ai_travel_planner/ui/home/plan/PlanScreen.java
package com.example.ai_travel_planner.ui.home.plan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.ui.home.plan.GuideScreen;

import java.util.List;

public class PlanScreen extends AppCompatActivity {

    private RecyclerView rvPlans;
    private TextView tvEmpty;
    private PlansAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planscreen);

        rvPlans = findViewById(R.id.rvPlans);
        tvEmpty = findViewById(R.id.tvSubtitle); // dùng tạm subtitle như empty hint

        // Lưới 1 cột (đổi 2 nếu bạn muốn kiểu gallery)
        rvPlans.setLayoutManager(new GridLayoutManager(this, 1));

        // Khi click 1 kế hoạch -> mở GuideScreen, truyền plan_id
        adapter = new PlansAdapter(plan -> {
            Intent it = new Intent(PlanScreen.this, GuideScreen.class);
            it.putExtra("plan_id", plan.id);
            startActivity(it);
        });
        rvPlans.setAdapter(adapter);
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

                // Ẩn/hiện empty hint + danh sách
                boolean empty = (all == null || all.isEmpty());
                tvEmpty.setText(empty ? "Chưa có lịch trình nào. Hãy tạo bằng AI nhé!" : "Your saved trips");
                rvPlans.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        }).start();
    }
}