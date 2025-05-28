package com.avdhaan;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.FocusSchedule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import com.google.android.material.appbar.MaterialToolbar;

public class ScheduleActivity extends BaseScheduleActivity implements ScheduleListAdapter.OnScheduleUpdated {

    private RecyclerView scheduleList;
    private ScheduleListAdapter adapter;
    private final ExecutorService executor = AppDatabase.databaseWriteExecutor;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_schedule;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        TextView titleText = findViewById(R.id.titleTextView);
        setSupportActionBar(toolbar);
        
        // Set title based on whether we're editing or adding
        int editScheduleIndex = getIntent().getIntExtra("editScheduleIndex", -1);
        String title = editScheduleIndex == -1 ? getString(R.string.add_schedule) : getString(R.string.edit_schedule);
        toolbar.setTitle(title);
        titleText.setText(title);
        
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        setupScheduleList();
    }

    private void setupScheduleList() {
        scheduleList = findViewById(R.id.scheduleList);
        scheduleList.setLayoutManager(new LinearLayoutManager(this));
        executor.execute(() -> {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            runOnUiThread(() -> {
                adapter = new ScheduleListAdapter(this, schedules, this);
                scheduleList.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onScheduleSaved() {
        executor.execute(() -> {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            runOnUiThread(() -> {
                adapter.updateSchedules(schedules);
                finish();
            });
        });
    }

    @Override
    public void onEdit(int position) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra("editScheduleIndex", position);
        startActivity(intent);
    }

    @Override
    public void onDelete(int position) {
        executor.execute(() -> {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            if (position >= 0 && position < schedules.size()) {
                schedules.remove(position);
                ScheduleStorage.saveSchedules(this, schedules);
                runOnUiThread(() -> adapter.updateSchedules(schedules));
            }
        });
    }

    @Override
    public void onScheduleUpdated() {
        executor.execute(() -> {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            runOnUiThread(() -> adapter.updateSchedules(schedules));
        });
    }
}