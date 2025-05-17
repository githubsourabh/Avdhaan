package com.avdhaan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private static final Map<Integer, Integer> DAY_ID_MAP = new HashMap<>();
    static {
        DAY_ID_MAP.put(R.id.toggleSun, Calendar.SUNDAY);
        DAY_ID_MAP.put(R.id.toggleMon, Calendar.MONDAY);
        DAY_ID_MAP.put(R.id.toggleTue, Calendar.TUESDAY);
        DAY_ID_MAP.put(R.id.toggleWed, Calendar.WEDNESDAY);
        DAY_ID_MAP.put(R.id.toggleThu, Calendar.THURSDAY);
        DAY_ID_MAP.put(R.id.toggleFri, Calendar.FRIDAY);
        DAY_ID_MAP.put(R.id.toggleSat, Calendar.SATURDAY);
    }

    private TimePicker startTimePicker, endTimePicker;
    private GridLayout dayToggleGrid;
    private AlarmManager alarmManager;

    private int editingScheduleIndex = -1;
    private FocusSchedule editingSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);
        startTimePicker.setIs24HourView(false);
        endTimePicker.setIs24HourView(false);

        dayToggleGrid = findViewById(R.id.toggleSun).getParent() instanceof GridLayout
                ? (GridLayout) findViewById(R.id.toggleSun).getParent()
                : null;

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Check for editing
        editingScheduleIndex = getIntent().getIntExtra("editScheduleIndex", -1);
        if (editingScheduleIndex != -1) {
            List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
            if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size()) {
                editingSchedule = schedules.get(editingScheduleIndex);
                prefillSchedule(editingSchedule);
            }
        }

        Button saveBtn = findViewById(R.id.saveScheduleBtn);
        saveBtn.setOnClickListener(v -> saveSchedule());
    }

    private void prefillSchedule(FocusSchedule schedule) {
        startTimePicker.setHour(schedule.startHour);
        startTimePicker.setMinute(schedule.startMinute);
        endTimePicker.setHour(schedule.endHour);
        endTimePicker.setMinute(schedule.endMinute);

        for (int i = 0; i < dayToggleGrid.getChildCount(); i++) {
            ToggleButton toggle = (ToggleButton) dayToggleGrid.getChildAt(i);
            int day = getDayOfWeekFromId(toggle.getId());
            toggle.setChecked(day == schedule.dayOfWeek);
        }
    }

    private void saveSchedule() {
        startTimePicker.clearFocus();
        endTimePicker.clearFocus();

        int startHour = getHour(startTimePicker);
        int startMinute = getMinute(startTimePicker);
        int endHour = getHour(endTimePicker);
        int endMinute = getMinute(endTimePicker);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            return;
        }

        if (!isValidTimeRange(startHour, startMinute, endHour, endMinute)) {
            Toast.makeText(this, getString(R.string.end_time_must_be_after_start), Toast.LENGTH_LONG).show();
            return;
        }

        boolean anyDaySelected = false;
        List<FocusSchedule> allSchedules = new ArrayList<>(ScheduleStorage.loadSchedules(this));

        for (int i = 0; i < dayToggleGrid.getChildCount(); i++) {
            ToggleButton toggle = (ToggleButton) dayToggleGrid.getChildAt(i);
            if (!toggle.isChecked()) continue;

            anyDaySelected = true;
            int dayOfWeek = getDayOfWeekFromId(toggle.getId());

            // Remove existing schedule if editing
            if (editingScheduleIndex != -1) {
                allSchedules.remove(editingScheduleIndex);
            }

            if (hasOverlappingSchedule(allSchedules, dayOfWeek, startHour, startMinute, endHour, endMinute)) {
                Toast.makeText(this, getString(R.string.schedule_overlaps_with_an_existing_one), Toast.LENGTH_LONG).show();
                return;
            }

            FocusSchedule newSchedule = new FocusSchedule(dayOfWeek, startHour, startMinute, endHour, endMinute);
            allSchedules.add(newSchedule);

            scheduleAlarms(newSchedule);
        }

        if (!anyDaySelected) {
            Toast.makeText(this, getString(R.string.select_at_least_one_day), Toast.LENGTH_LONG).show();
            return;
        }

        ScheduleStorage.saveSchedules(this, allSchedules);
        Toast.makeText(this, getString(R.string.schedule_saved), Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean isValidTimeRange(int startHour, int startMinute, int endHour, int endMinute) {
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.HOUR_OF_DAY, startHour);
        startCal.set(Calendar.MINUTE, startMinute);

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, endMinute);

        return endCal.after(startCal);
    }

    private boolean hasOverlappingSchedule(List<FocusSchedule> schedules, int dayOfWeek,
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

    private void scheduleAlarms(FocusSchedule schedule) {
        scheduleAlarm(schedule.dayOfWeek, schedule.startHour, schedule.startMinute, FocusAlarmReceiver.class);
        scheduleAlarm(schedule.dayOfWeek, schedule.endHour, schedule.endMinute, FocusEndReceiver.class);
    }

    private void scheduleAlarm(int dayOfWeek, int hour, int minute, Class<?> receiverClass) {
        Intent alarmIntent = new Intent(this, receiverClass);
        alarmIntent.putExtra("day", dayOfWeek);

        int requestCode = (receiverClass == FocusEndReceiver.class ? 1000 : 0) + dayOfWeek;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private int getHour(TimePicker picker) {
        return Build.VERSION.SDK_INT >= 23 ? picker.getHour() : picker.getCurrentHour();
    }

    private int getMinute(TimePicker picker) {
        return Build.VERSION.SDK_INT >= 23 ? picker.getMinute() : picker.getCurrentMinute();
    }

    private int getDayOfWeekFromId(int id) {
        return DAY_ID_MAP.getOrDefault(id, -1);
    }
}
