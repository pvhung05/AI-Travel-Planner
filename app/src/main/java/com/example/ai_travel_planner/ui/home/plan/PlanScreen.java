// app/src/main/java/com/example/ai_travel_planner/ui/home/plan/PlanScreen.java
package com.example.ai_travel_planner.ui.home.plan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.data.local.AppDatabase;
import com.example.ai_travel_planner.data.local.entities.PlanEntity;
import com.example.ai_travel_planner.ui.home.login.SigninScreen;
import com.example.ai_travel_planner.ui.home.plan.GuideScreen;

import java.util.List;

public class PlanScreen extends AppCompatActivity {

    private RecyclerView rvPlans;
    private TextView tvEmpty;
    private PlansAdapter adapter;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planscreen);

        rvPlans = findViewById(R.id.rvPlans);
        tvEmpty = findViewById(R.id.tvSubtitle); // dùng tạm subtitle làm empty hint

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish()); // ← QUAY LẠI, KHÔNG mở GuideScreen
        }

        rvPlans.setLayoutManager(new GridLayoutManager(this, 1));

        adapter = new PlansAdapter(plan -> {
            // Mở chi tiết kế hoạch, PHẢI truyền plan_id
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
                boolean empty = (all == null || all.isEmpty());
                tvEmpty.setText(empty ? "Chưa có lịch trình nào. Hãy tạo bằng AI nhé!" : "Your saved trips");
                rvPlans.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        }).start();
    }
}