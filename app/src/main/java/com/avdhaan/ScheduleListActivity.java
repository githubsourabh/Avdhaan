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

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.FocusSchedule;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import com.google.android.material.appbar.MaterialToolbar;

public class ScheduleListActivity extends AppCompatActivity {

    private ScheduleListAdapter adapter;
    private RecyclerView scheduleList;
    private List<FocusSchedule> schedules;
    private TextView emptyText;
    private final ExecutorService executor = AppDatabase.databaseWriteExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

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
                    .setTitle(R.string.BTN_CLR_ALL_SCH)
                    .setMessage(R.string.TXT_CONF_DEL_ALL_SCH)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        executor.execute(() -> {
                            ScheduleStorage.saveSchedules(this, List.of());
                            runOnUiThread(this::loadAndDisplaySchedules);
                        });
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
        executor.execute(() -> {
            schedules = ScheduleStorage.loadSchedules(this);

            runOnUiThread(() -> {
                Button clearAllBtn = findViewById(R.id.btn_clear_all_schedules);

                if (schedules.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    scheduleList.setVisibility(View.GONE);
                    clearAllBtn.setVisibility(View.GONE);
                    return;
                }

                emptyText.setVisibility(View.GONE);
                scheduleList.setVisibility(View.VISIBLE);
                clearAllBtn.setVisibility(View.VISIBLE);

                adapter = new ScheduleListAdapter(this, schedules, new ScheduleListAdapter.OnScheduleUpdated() {
                    @Override
                    public void onEdit(int position) {
                        Intent intent = new Intent(ScheduleListActivity.this, ScheduleActivity.class);
                        intent.putExtra("editScheduleIndex", position);
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(int position) {
                        executor.execute(() -> {
                            schedules.remove(position);
                            ScheduleStorage.saveSchedules(ScheduleListActivity.this, schedules);
                            runOnUiThread(() -> {
                                adapter.notifyItemRemoved(position);
                                loadAndDisplaySchedules();
                            });
                        });
                    }

                    @Override
                    public void onScheduleUpdated() {
                        executor.execute(() -> {
                            List<FocusSchedule> updatedSchedules = ScheduleStorage.loadSchedules(ScheduleListActivity.this);
                            runOnUiThread(() -> adapter.updateSchedules(updatedSchedules));
                        });
                    }
                });

                scheduleList.setAdapter(adapter);
            });
        });
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_schedule))
                .setMessage(getString(R.string.delete_schedule_confirm))
                .setPositiveButton("Delete", (dialog, which) -> {
                    executor.execute(() -> {
                        schedules.remove(position);
                        ScheduleStorage.saveSchedules(this, schedules);
                        runOnUiThread(() -> {
                            adapter.notifyItemRemoved(position);
                            loadAndDisplaySchedules();
                        });
                    });
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}