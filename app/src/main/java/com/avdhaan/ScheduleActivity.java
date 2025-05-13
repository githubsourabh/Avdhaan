
package com.avdhaan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;
import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity {

    private Button startTimeBtn, endTimeBtn, saveBtn;
    private String startTime = "", endTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        startTimeBtn = findViewById(R.id.start_time_btn);
        endTimeBtn = findViewById(R.id.end_time_btn);
        saveBtn = findViewById(R.id.save_btn);

        startTimeBtn.setOnClickListener(v -> pickTime(true));
        endTimeBtn.setOnClickListener(v -> pickTime(false));
        saveBtn.setOnClickListener(v -> saveSchedule());
    }

    private void pickTime(boolean isStart) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this,
            (view, hourOfDay, minute) -> {
                String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                if (isStart) {
                    startTime = timeStr;
                    startTimeBtn.setText("Start Time: " + timeStr);
                } else {
                    endTime = timeStr;
                    endTimeBtn.setText("End Time: " + timeStr);
                }
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true);
        dialog.show();
    }

    private void saveSchedule() {
        Set<String> selectedDays = new HashSet<>();
        int[] dayIds = {
            R.id.checkbox_mon, R.id.checkbox_tue, R.id.checkbox_wed,
            R.id.checkbox_thu, R.id.checkbox_fri, R.id.checkbox_sat, R.id.checkbox_sun
        };
        String[] dayNames = {
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
        };

        for (int i = 0; i < dayIds.length; i++) {
            CheckBox cb = findViewById(dayIds[i]);
            if (cb.isChecked()) {
                selectedDays.add(dayNames[i]);
            }
        }

        if (selectedDays.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("focus_schedule", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("days", selectedDays);
        editor.putString("start", startTime);
        editor.putString("end", endTime);
        editor.apply();

        // Register AlarmManager for each selected day
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        for (String day : selectedDays) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime.split(":")[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(startTime.split(":")[1]));
            calendar.set(Calendar.SECOND, 0);

            // Adjust day of week
            int dayOfWeek = getCalendarDayFromName(day);
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1); // ensure future date
            }

            Intent alarmIntent = new Intent(this, FocusAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, dayOfWeek, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
        }


        Toast.makeText(this, "Schedule saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Helper
    private int getCalendarDayFromName(String name) {
        switch (name) {
            case "Sunday": return Calendar.SUNDAY;
            case "Monday": return Calendar.MONDAY;
            case "Tuesday": return Calendar.TUESDAY;
            case "Wednesday": return Calendar.WEDNESDAY;
            case "Thursday": return Calendar.THURSDAY;
            case "Friday": return Calendar.FRIDAY;
            case "Saturday": return Calendar.SATURDAY;
            default: return Calendar.MONDAY;
        }
    }
}
