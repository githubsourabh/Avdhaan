package com.avdhaan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleListActivity extends AppCompatActivity {

    private ScheduleListAdapter adapter;
    private RecyclerView scheduleList;
    private List<FocusSchedule> schedules;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        scheduleList = findViewById(R.id.schedule_list);
        Button addScheduleButton = findViewById(R.id.btn_add_schedule);
        emptyText = findViewById(R.id.empty_text);

        addScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleActivity.class);
            startActivity(intent);
        });

        Button clearAllButton = findViewById(R.id.btn_clear_all_schedules);
        clearAllButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Schedules")
                    .setMessage("Are you sure you want to delete all schedules?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        ScheduleStorage.saveSchedules(this, List.of());
                        loadAndDisplaySchedules();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        scheduleList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplaySchedules();
    }

    private void loadAndDisplaySchedules() {
        schedules = ScheduleStorage.loadSchedules(this);

        Button clearAllBtn = findViewById(R.id.btn_clear_all_schedules);

        if (schedules.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            scheduleList.setVisibility(View.GONE);
            clearAllBtn.setVisibility(View.GONE);  // 🔴 Hide button if no schedules
            return;
        }

        emptyText.setVisibility(View.GONE);
        scheduleList.setVisibility(View.VISIBLE);
        clearAllBtn.setVisibility(View.VISIBLE);  // ✅ Show button if schedules exist

        adapter = new ScheduleListAdapter(this, schedules, new ScheduleListAdapter.OnScheduleUpdated() {
            @Override
            public void onEdit(int position) {
                Intent intent = new Intent(ScheduleListActivity.this, ScheduleActivity.class);
                intent.putExtra("editScheduleIndex", position);
                startActivity(intent);
            }

            @Override
            public void onDelete(int position) {
                schedules.remove(position);
                ScheduleStorage.saveSchedules(ScheduleListActivity.this, schedules);
                adapter.notifyItemRemoved(position);
                loadAndDisplaySchedules();  // Refresh after delete
            }
        });

        scheduleList.setLayoutManager(new LinearLayoutManager(this));
        scheduleList.setAdapter(adapter);
    }


    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    schedules.remove(position);
                    ScheduleStorage.saveSchedules(this, schedules);
                    adapter.notifyItemRemoved(position);
                    loadAndDisplaySchedules();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
