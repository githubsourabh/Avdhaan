package com.avdhaan;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseScheduleActivity extends AppCompatActivity {

    protected static final Map<Integer, Integer> DAY_ID_MAP = new HashMap<>();
    static {
        DAY_ID_MAP.put(R.id.toggleSun, Calendar.SUNDAY);
        DAY_ID_MAP.put(R.id.toggleMon, Calendar.MONDAY);
        DAY_ID_MAP.put(R.id.toggleTue, Calendar.TUESDAY);
        DAY_ID_MAP.put(R.id.toggleWed, Calendar.WEDNESDAY);
        DAY_ID_MAP.put(R.id.toggleThu, Calendar.THURSDAY);
        DAY_ID_MAP.put(R.id.toggleFri, Calendar.FRIDAY);
        DAY_ID_MAP.put(R.id.toggleSat, Calendar.SATURDAY);
    }

    protected TimePicker startTimePicker, endTimePicker;
    protected GridLayout dayToggleGrid;
    protected Button saveButton;

    protected int editingScheduleIndex = -1;
    protected FocusSchedule editingSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        initializeViews();
        setupTimePickers();
        setupDayToggles();
        setupSaveButton();

        // Check for editing
        editingScheduleIndex = getIntent().getIntExtra("editScheduleIndex", -1);
        if (editingScheduleIndex != -1) {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size()) {
                editingSchedule = schedules.get(editingScheduleIndex);
                prefillSchedule(editingSchedule);
            }
        }
    }

    protected abstract int getLayoutResourceId();

    protected void initializeViews() {
        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);
        dayToggleGrid = findViewById(R.id.toggleSun).getParent() instanceof GridLayout
                ? (GridLayout) findViewById(R.id.toggleSun).getParent()
                : null;
        saveButton = findViewById(R.id.saveScheduleBtn);
    }

    protected void setupTimePickers() {
        startTimePicker.setIs24HourView(false);
        endTimePicker.setIs24HourView(false);
    }

    protected void setupDayToggles() {
        // Override in subclasses if needed
    }

    protected void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveSchedule());
    }

    protected void prefillSchedule(FocusSchedule schedule) {
        startTimePicker.setHour(schedule.startHour);
        startTimePicker.setMinute(schedule.startMinute);
        endTimePicker.setHour(schedule.endHour);
        endTimePicker.setMinute(schedule.endMinute);

        for (int i = 0; i < dayToggleGrid.getChildCount(); i++) {
            android.widget.ToggleButton toggle = (android.widget.ToggleButton) dayToggleGrid.getChildAt(i);
            int day = getDayOfWeekFromId(toggle.getId());
            toggle.setChecked(day == schedule.dayOfWeek);
        }
    }

    protected void saveSchedule() {
        Log.d("BaseScheduleActivity", "saveSchedule called");
        startTimePicker.clearFocus();
        endTimePicker.clearFocus();

        int startHour = getHour(startTimePicker);
        int startMinute = getMinute(startTimePicker);
        int endHour = getHour(endTimePicker);
        int endMinute = getMinute(endTimePicker);

        Log.d("BaseScheduleActivity", "Time selected - Start: " + startHour + ":" + startMinute + 
              ", End: " + endHour + ":" + endMinute);

        if (!isValidTimeRange(startHour, startMinute, endHour, endMinute)) {
            Log.d("BaseScheduleActivity", "Invalid time range");
            Toast.makeText(this, getString(R.string.end_time_must_be_after_start), Toast.LENGTH_LONG).show();
            return;
        }

        boolean anyDaySelected = false;
        List<FocusSchedule> allSchedules = new ArrayList<>(ScheduleStorage.loadSchedules(this));
        Log.d("BaseScheduleActivity", "Current schedules count: " + allSchedules.size());

        for (int i = 0; i < dayToggleGrid.getChildCount(); i++) {
            android.widget.ToggleButton toggle = (android.widget.ToggleButton) dayToggleGrid.getChildAt(i);
            if (!toggle.isChecked()) continue;

            anyDaySelected = true;
            int dayOfWeek = getDayOfWeekFromId(toggle.getId());
            Log.d("BaseScheduleActivity", "Processing day: " + dayOfWeek);

            if (hasOverlappingSchedule(allSchedules, dayOfWeek, startHour, startMinute, endHour, endMinute)) {
                Log.d("BaseScheduleActivity", "Schedule overlaps with existing one");
                Toast.makeText(this, getString(R.string.schedule_overlaps_with_an_existing_one), Toast.LENGTH_LONG).show();
                return;
            }

            FocusSchedule newSchedule = new FocusSchedule(dayOfWeek, startHour, startMinute, endHour, endMinute);
            allSchedules.add(newSchedule);
            Log.d("BaseScheduleActivity", "Added new schedule for day: " + dayOfWeek);
        }

        if (!anyDaySelected) {
            Log.d("BaseScheduleActivity", "No days selected");
            Toast.makeText(this, getString(R.string.select_at_least_one_day), Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("BaseScheduleActivity", "Saving " + allSchedules.size() + " schedules");
        ScheduleStorage.saveSchedules(this, allSchedules);
        Toast.makeText(this, getString(R.string.schedule_saved), Toast.LENGTH_SHORT).show();
        onScheduleSaved();
    }

    protected boolean isValidTimeRange(int startHour, int startMinute, int endHour, int endMinute) {
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.HOUR_OF_DAY, startHour);
        startCal.set(Calendar.MINUTE, startMinute);

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, endMinute);

        return endCal.after(startCal);
    }

    protected boolean hasOverlappingSchedule(List<FocusSchedule> schedules, int dayOfWeek,
                                           int startHour, int startMinute, int endHour, int endMinute) {
        int start1 = startHour * 60 + startMinute;
        int end1 = endHour * 60 + endMinute;

        for (FocusSchedule existing : schedules) {
            if (existing.dayOfWeek != dayOfWeek) continue;

            int start2 = existing.startHour * 60 + existing.startMinute;
            int end2 = existing.endHour * 60 + existing.endMinute;

            if (Math.max(start1, start2) < Math.min(end1, end2)) {
                return true;
            }
        }
        return false;
    }

    protected int getHour(TimePicker picker) {
        return Build.VERSION.SDK_INT >= 23 ? picker.getHour() : picker.getCurrentHour();
    }

    protected int getMinute(TimePicker picker) {
        return Build.VERSION.SDK_INT >= 23 ? picker.getMinute() : picker.getCurrentMinute();
    }

    protected int getDayOfWeekFromId(int id) {
        return DAY_ID_MAP.getOrDefault(id, -1);
    }

    protected abstract void onScheduleSaved();
} 