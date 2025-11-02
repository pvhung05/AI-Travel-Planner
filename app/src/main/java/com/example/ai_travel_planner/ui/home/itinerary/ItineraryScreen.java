package com.example.ai_travel_planner.ui.home.itinerary;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.models.ActivityItem;
import com.example.ai_travel_planner.network.models.DayPlan;
import com.example.ai_travel_planner.network.models.TripPlan;
import com.example.ai_travel_planner.ui.home.friend.FriendScreen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItineraryScreen extends AppCompatActivity implements ItineraryAdapter.Callbacks {

    private RecyclerView recycler;
    private Button btnDeleteSelected, btnAddActivity, btnSavePlan;
    private View btnNext, btnClearDay; // ImageButton hoặc TextView, tuỳ layout
    private LinearLayout dayTabs;

    private TripPlan plan;
    private DayPlan currentDay;
    private final Set<String> selectedIds = new HashSet<>();
    private ItineraryAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_itinerary);

        // Views
        recycler = findViewById(R.id.recyclerActivities);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnAddActivity = findViewById(R.id.btnAddActivity);
        btnSavePlan = findViewById(R.id.btnSavePlan);
        btnClearDay = findViewById(R.id.btnClearDay);
        dayTabs = findViewById(R.id.dayTabs);

        btnNext = findViewById(R.id.btnNext);
        if (btnNext == null) {
            // fallback nếu layout cũ còn btnBack
            btnNext = findViewById(R.id.btnBack);
        }

        // Adapter
        adapter = new ItineraryAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // Load plan từ Intent (Serializable hoặc JSON string)
        Object tp = getIntent().getSerializableExtra("tripPlan");
        if (tp instanceof TripPlan) {
            plan = (TripPlan) tp;
        } else {
            String json = getIntent().getStringExtra("plan_text");
            if (json != null) {
                try {
                    plan = new com.google.gson.Gson().fromJson(json, TripPlan.class);
                } catch (Exception ignore) { }
            }
        }
        if (plan == null || plan.days == null || plan.days.isEmpty()) {
            Toast.makeText(this, "No plan data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Chọn Day 1 mặc định
        currentDay = plan.days.get(0);
        bindDayTabs();
        bindList();

        // Hành vi nút
        btnAddActivity.setOnClickListener(v -> openEditor(null));
        btnDeleteSelected.setOnClickListener(v -> deleteSelected());
        btnDeleteSelected.setOnLongClickListener(v -> { confirmClearDay(); return true; });
        btnSavePlan.setOnClickListener(v -> savePlanAndFinish());
        if (btnClearDay != null) {
            btnClearDay.setOnClickListener(v -> confirmClearDay());
        }
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(ItineraryScreen.this, FriendScreen.class);
            intent.putExtra("tripPlan", (Serializable) plan);
        })
    }

    /** Vẽ tab cho từng ngày, cho phép chuyển ngày và giữ lựa chọn đúng */
    private void bindDayTabs() {
        dayTabs.removeAllViews();
        for (int i = 0; i < plan.days.size(); i++) {
            final int idx = i;
            final DayPlan dp = plan.days.get(i);

            TextView tv = new TextView(this);
            tv.setText((dp.label != null && !dp.label.isEmpty()) ? dp.label : ("Day " + (i + 1)));
            tv.setTextSize(16);
            int padH = (int) (16 * getResources().getDisplayMetrics().density);
            int padV = (int) (8 * getResources().getDisplayMetrics().density);
            tv.setPadding(padH, padV, padH, padV);

            boolean selected = (dp == currentDay);
            tv.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
            tv.setTextColor(selected ? 0xFFEF6C00 : 0xFF6B6B6B); // cam đậm khi chọn, xám khi không

            tv.setOnClickListener(v -> {
                selectedIds.clear();
                currentDay = dp;
                bindList();
            });
            dayTabs.addView(tv);

            if (i != plan.days.size() - 1) {
                Space sp = new Space(this);
                sp.setMinimumWidth(padH / 2);
                dayTabs.addView(sp);
            }
        }
    }

    /** Bind danh sách activity của ngày hiện tại */
    private void bindList() {
        if (currentDay.activities == null) currentDay.activities = new ArrayList<>();
        adapter.submitList(new ArrayList<>(currentDay.activities));
        updateDeleteButton();
        // Làm mới tab highlight
        bindDayTabs();
    }

    private void updateDeleteButton() {
        boolean enabled = !selectedIds.isEmpty();
        btnDeleteSelected.setEnabled(enabled);
        btnDeleteSelected.setBackgroundResource(
                enabled ? R.drawable.bg_button_outlined_red : R.drawable.bg_button_disabled
        );
    }

    /** Xoá các activity đã tick chọn */
    private void deleteSelected() {
        if (selectedIds.isEmpty()) return;
        List<ActivityItem> remain = new ArrayList<>();
        for (ActivityItem a : currentDay.activities) {
            if (a.id == null || !selectedIds.contains(a.id)) remain.add(a);
        }
        currentDay.activities.clear();
        currentDay.activities.addAll(remain);
        selectedIds.clear();
        adapter.clearSelection();
        adapter.submitList(new ArrayList<>(currentDay.activities));
        updateDeleteButton();
    }

    /** Xác nhận & xoá toàn bộ hoạt động trong ngày hiện tại */
    private void confirmClearDay() {
        new AlertDialog.Builder(this)
                .setTitle("Xoá toàn bộ hoạt động ngày này?")
                .setMessage("Thao tác này không thể hoàn tác.")
                .setPositiveButton("Xoá", (d, w) -> {
                    if (currentDay != null && currentDay.activities != null) {
                        currentDay.activities.clear();
                        selectedIds.clear();
                        adapter.clearSelection();
                        adapter.submitList(new ArrayList<>(currentDay.activities));
                        updateDeleteButton();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /** Lưu & thoát (bạn có thể thay bằng lưu Room) */
    private void savePlanAndFinish() {
        AiPlanRepository repo = AiPlanRepository.getInstance(getApplicationContext());
        repo.savePlan(plan, new AiPlanRepository.SaveCallback() {
            @Override public void onSaved(long id) {
                // quay về GuideScreen
                setResult(RESULT_OK);
                Toast.makeText(ItineraryScreen.this, "Plan saved", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(Exception e) {
                Toast.makeText(ItineraryScreen.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== Adapter callbacks =====
    @Override public void onItemClick(ActivityItem item) {
        openEditor(item);
    }

    @Override public void onSelectionChanged(Set<String> ids) {
        selectedIds.clear();
        if (ids != null) selectedIds.addAll(ids);
        updateDeleteButton();
    }

    // ===== Editor đơn giản (dialog) – không phụ thuộc BottomSheet, đảm bảo luôn chạy =====
    private void openEditor(@Nullable ActivityItem item) {
        final boolean isNew = (item == null);
        final ActivityItem working = (item == null) ? new ActivityItem() : cloneItem(item);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, pad);

        final EditText etTime = new EditText(this);
        etTime.setHint("Time (e.g. 09:00)");
        etTime.setInputType(InputType.TYPE_CLASS_DATETIME);
        etTime.setText(working.time == null ? "" : working.time);
        box.addView(etTime);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Title");
        etTitle.setText(working.title == null ? "" : working.title);
        box.addView(etTitle);

        final EditText etCat = new EditText(this);
        etCat.setHint("Category (food/nature/culture/...)");
        etCat.setText(working.category == null ? "" : working.category);
        box.addView(etCat);

        new AlertDialog.Builder(this)
                .setTitle(isNew ? "Add activity" : "Edit activity")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    working.time = safe(etTime.getText().toString());
                    working.title = safe(etTitle.getText().toString());
                    working.category = safe(etCat.getText().toString());
                    if (isNew) {
                        working.id = UUID.randomUUID().toString();
                        currentDay.activities.add(working);
                    } else {
                        for (int i = 0; i < currentDay.activities.size(); i++) {
                            ActivityItem ai = currentDay.activities.get(i);
                            if (ai.id != null && ai.id.equals(item.id)) {
                                currentDay.activities.set(i, working);
                                break;
                            }
                        }
                    }
                    sortByTime(currentDay.activities);
                    adapter.submitList(new ArrayList<>(currentDay.activities));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private ActivityItem cloneItem(ActivityItem a) {
        ActivityItem b = new ActivityItem();
        b.id = a.id;
        b.time = a.time;
        b.title = a.title;
        b.category = a.category;
        return b;
    }

    private void sortByTime(List<ActivityItem> list) {
        list.sort((a, b) -> safe(a.time).compareTo(safe(b.time)));
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
