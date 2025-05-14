package com.avdhaan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.avdhaan.FocusSchedule;
import com.avdhaan.ScheduleStorage;


public class ScheduleActivity extends AppCompatActivity {

    private TimePicker startTimePicker, endTimePicker;
    private TextView scheduleStatus;
    private GridLayout dayToggleGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);

        startTimePicker.setIs24HourView(false);
        endTimePicker.setIs24HourView(false);

        scheduleStatus = findViewById(R.id.scheduleStatus);
        dayToggleGrid = findViewById(R.id.toggleSun).getParent() instanceof GridLayout
                ? (GridLayout) findViewById(R.id.toggleSun).getParent()
                : null;

        Button saveBtn = findViewById(R.id.saveScheduleBtn);
        saveBtn.setOnClickListener(v -> saveSchedule());

        displaySavedSchedules();

        Button clearBtn = findViewById(R.id.clearScheduleBtn);
        clearBtn.setOnClickListener(v -> clearAllSchedules());
    }

    private void saveSchedule() {
        startTimePicker.clearFocus();
        endTimePicker.clearFocus();

        int startHour = getHour(startTimePicker);
        int startMinute = getMinute(startTimePicker);
        int endHour = getHour(endTimePicker);
        int endMinute = getMinute(endTimePicker);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.HOUR_OF_DAY, startHour);
        startCal.set(Calendar.MINUTE, startMinute);
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, endMinute);
        endCal.set(Calendar.SECOND, 0);

        if (!endCal.after(startCal)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_LONG).show();
            return;
        }

        boolean anyDaySelected = false;
        StringBuilder summary = new StringBuilder("Focus Mode Scheduled:\n");

        List<FocusSchedule> currentSchedules = ScheduleStorage.loadSchedules(this);

        for (int i = 0; i < dayToggleGrid.getChildCount(); i++) {
            ToggleButton toggle = (ToggleButton) dayToggleGrid.getChildAt(i);
            if (toggle.isChecked()) {
                anyDaySelected = true;
                int dayOfWeek = getDayOfWeekFromId(toggle.getId());

                FocusSchedule schedule = new FocusSchedule(dayOfWeek, startHour, startMinute, endHour, endMinute);
                currentSchedules.add(schedule);

                summary.append(toggle.getText()).append(" ")
                        .append(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute))
                        .append(" to ")
                        .append(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute))
                        .append("\n");
            }
        }

        if (!anyDaySelected) {
            Toast.makeText(this, "Select at least one day", Toast.LENGTH_LONG).show();
            return;
        }

        ScheduleStorage.saveSchedules(this, currentSchedules);
        scheduleStatus.setText(summary.toString());
    }

    private void scheduleAlarm(int dayOfWeek, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, FocusAlarmReceiver.class);
        alarmIntent.putExtra("day", dayOfWeek);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, dayOfWeek, alarmIntent,
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

    private void scheduleEndAlarm(int dayOfWeek, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, FocusEndReceiver.class);
        alarmIntent.putExtra("day", dayOfWeek);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 1000 + dayOfWeek,
                alarmIntent,
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
        if (id == R.id.toggleSun) return Calendar.SUNDAY;
        else if (id == R.id.toggleMon) return Calendar.MONDAY;
        else if (id == R.id.toggleTue) return Calendar.TUESDAY;
        else if (id == R.id.toggleWed) return Calendar.WEDNESDAY;
        else if (id == R.id.toggleThu) return Calendar.THURSDAY;
        else if (id == R.id.toggleFri) return Calendar.FRIDAY;
        else if (id == R.id.toggleSat) return Calendar.SATURDAY;
        else return -1;
    }

    private void displaySavedSchedules() {
        List<FocusSchedule> savedSchedules = ScheduleStorage.loadSchedules(this);

        if (savedSchedules.isEmpty()) {
            scheduleStatus.setText("No schedule set yet");
            return;
        }

        StringBuilder summary = new StringBuilder("Saved Schedules:\n");
        for (FocusSchedule schedule : savedSchedules) {
            String dayName = getDayName(schedule.getDayOfWeek());
            String start = String.format(Locale.getDefault(), "%02d:%02d", schedule.getStartHour(), schedule.getStartMinute());
            String end = String.format(Locale.getDefault(), "%02d:%02d", schedule.getEndHour(), schedule.getEndMinute());
            summary.append(dayName).append(": ").append(start).append(" - ").append(end).append("\n");
        }

        scheduleStatus.setText(summary.toString());
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "Unknown";
        }
    }

    private void clearAllSchedules() {
        List<FocusSchedule> schedules = ScheduleStorage.loadSchedules(this);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for (FocusSchedule schedule : schedules) {
            Intent startIntent = new Intent(this, FocusAlarmReceiver.class);
            PendingIntent startPending = PendingIntent.getBroadcast(this, schedule.getDayOfWeek(), startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(startPending);

            Intent endIntent = new Intent(this, FocusEndReceiver.class);
            PendingIntent endPending = PendingIntent.getBroadcast(this, 1000 + schedule.getDayOfWeek(), endIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(endPending);
        }

        ScheduleStorage.saveSchedules(this, new ArrayList<>());
        scheduleStatus.setText("All schedules cleared.");
    }
}