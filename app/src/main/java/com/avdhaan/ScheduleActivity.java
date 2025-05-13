// ScheduleActivity.java
package com.avdhaan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private ToggleButton[] dayToggles;
    private Button saveBtn;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        timePicker = findViewById(R.id.timePicker);
        dayToggles = new ToggleButton[]{
                findViewById(R.id.toggleSun),
                findViewById(R.id.toggleMon),
                findViewById(R.id.toggleTue),
                findViewById(R.id.toggleWed),
                findViewById(R.id.toggleThu),
                findViewById(R.id.toggleFri),
                findViewById(R.id.toggleSat)
        };
        saveBtn = findViewById(R.id.saveScheduleBtn);
        statusText = findViewById(R.id.scheduleStatus);

        saveBtn.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            for (int i = 0; i < 7; i++) {
                if (dayToggles[i].isChecked()) {
                    scheduleFocus(hour, minute, i + 1);
                }
            }

            statusText.setText("Schedule set for selected days at " + hour + ":" + (minute < 10 ? "0" + minute : minute));
            Toast.makeText(this, "Focus mode scheduled!", Toast.LENGTH_SHORT).show();
        });
    }

    private void scheduleFocus(int hour, int minute, int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        Intent alarmIntent = new Intent(this, FocusAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, dayOfWeek, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
        }
    }
}
