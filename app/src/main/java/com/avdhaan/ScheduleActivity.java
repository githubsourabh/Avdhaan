package com.avdhaan;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScheduleActivity extends BaseScheduleActivity implements ScheduleListAdapter.OnScheduleUpdated {

    private RecyclerView scheduleList;
    private ScheduleListAdapter adapter;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_schedule;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupScheduleList();
    }

    private void setupScheduleList() {
        scheduleList = findViewById(R.id.scheduleList);
        scheduleList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleListAdapter(this, ScheduleStorage.loadSchedules(this), this);
        scheduleList.setAdapter(adapter);
    }

    @Override
    protected void onScheduleSaved() {
        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
        adapter.updateSchedules(schedules);
    }

    @Override
    public void onEdit(int position) {
        // Handle edit
        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
        if (position >= 0 && position < schedules.size()) {
            FocusSchedule schedule = schedules.get(position);
            // TODO: Implement edit functionality
        }
    }

    @Override
    public void onDelete(int position) {
        // Handle delete
        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
        if (position >= 0 && position < schedules.size()) {
            schedules.remove(position);
            ScheduleStorage.saveSchedules(this, schedules);
            adapter.updateSchedules(schedules);
        }
    }

    @Override
    public void onScheduleUpdated() {
        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
        adapter.updateSchedules(schedules);
    }
} 