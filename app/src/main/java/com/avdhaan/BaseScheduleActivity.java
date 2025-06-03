package com.avdhaan;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.FocusSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public abstract class BaseScheduleActivity extends AppCompatActivity {

    private static final String TAG = "BaseScheduleActivity";
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

    private final ExecutorService executor = AppDatabase.databaseWriteExecutor;

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
            executor.execute(() -> {
                List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
                if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size()) {
                    editingSchedule = schedules.get(editingScheduleIndex);
                    runOnUiThread(() -> prefillSchedule(editingSchedule));
                }
            });
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
        startTimePicker.setHour(schedule.getStartHour());
        startTimePicker.setMinute(schedule.getStartMinute());
        endTimePicker.setHour(schedule.getEndHour());
        endTimePicker.setMinute(schedule.getEndMinute());

        for (int i = 0; i < dayToggleGrid.getChildCount(); i++) {
            android.widget.ToggleButton toggle = (android.widget.ToggleButton) dayToggleGrid.getChildAt(i);
            int day = getDayOfWeekFromId(toggle.getId());
            toggle.setChecked(day == schedule.getDayOfWeek());
        }
    }

    protected void saveSchedule() {
        Log.d(TAG, "saveSchedule called");
        startTimePicker.clearFocus();
        endTimePicker.clearFocus();

        int startHour = getHour(startTimePicker);
        int startMinute = getMinute(startTimePicker);
        int endHour = getHour(endTimePicker);
        int endMinute = getMinute(endTimePicker);

        Log.d(TAG, "Time selected - Start: " + startHour + ":" + startMinute + 
              ", End: " + endHour + ":" + endMinute);

        if (!isValidTimeRange(startHour, startMinute, endHour, endMinute)) {
            Log.d(TAG, "Invalid time range");
            Toast.makeText(this, getString(R.string.end_time_must_be_after_start), Toast.LENGTH_LONG).show();
            return;
        }

        List<Integer> selectedDays = getSelectedDays();
        if (selectedDays.isEmpty()) {
            Log.d(TAG, "No days selected");
            Toast.makeText(this, getString(R.string.select_at_least_one_day), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            executor.execute(() -> {
                try {
                    // Get current schedules
                    List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
                    
                    // Check for overlaps
                    boolean hasOverlap = false;
                    for (int dayOfWeek : selectedDays) {
                        if (hasOverlappingSchedule(schedules, dayOfWeek, startHour, startMinute, endHour, endMinute)) {
                            hasOverlap = true;
                            break;
                        }
                    }

                    if (hasOverlap) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Schedule overlaps with existing one");
                            Toast.makeText(this, getString(R.string.schedule_overlaps_with_an_existing_one), Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    // Create new schedules for each selected day
                    List<FocusSchedule> newSchedules = new ArrayList<>();
                    for (int dayOfWeek : selectedDays) {
                        FocusSchedule schedule = new FocusSchedule();
                        schedule.setDayOfWeek(dayOfWeek);
                        schedule.setStartHour(startHour);
                        schedule.setStartMinute(startMinute);
                        schedule.setEndHour(endHour);
                        schedule.setEndMinute(endMinute);
                        schedule.setGroupId(1); // Default group
                        newSchedules.add(schedule);
                    }

                    // Update or add schedules
                    if (editingScheduleIndex >= 0) {
                        // Remove the old schedule
                        schedules.remove(editingScheduleIndex);
                        // Add all new schedules
                        schedules.addAll(newSchedules);
                    } else {
                        schedules.addAll(newSchedules);
                    }

                    // Save to database
                    ScheduleStorage.saveSchedules(this, schedules);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Schedule saved successfully", Toast.LENGTH_SHORT).show();
                        onScheduleSaved();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error saving schedule", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error saving schedule. Please try again.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (RejectedExecutionException e) {
            Log.e(TAG, "Executor rejected task", e);
            Toast.makeText(this, "Error saving schedule. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    protected List<Integer> getSelectedDays() {
        List<Integer> selectedDays = new ArrayList<>();
        for (int i = 0; i < dayToggleGrid.getChildCount(); i++) {
            android.widget.ToggleButton toggle = (android.widget.ToggleButton) dayToggleGrid.getChildAt(i);
            if (toggle.isChecked()) {
                selectedDays.add(getDayOfWeekFromId(toggle.getId()));
            }
        }
        return selectedDays;
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

        for (int i = 0; i < schedules.size(); i++) {
            // Skip the schedule being edited
            if (i == editingScheduleIndex) continue;
            
            FocusSchedule existing = schedules.get(i);
            if (existing.getDayOfWeek() != dayOfWeek) continue;

            int start2 = existing.getStartHour() * 60 + existing.getStartMinute();
            int end2 = existing.getEndHour() * 60 + existing.getEndMinute();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't shutdown the executor as it's shared across the app
    }
}