package com.avdhaan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ScheduleViewHolder> {

    public interface OnScheduleUpdated {
        void onEdit(int position);
        void onDelete(int position);
    }

    private final List<FocusSchedule> schedules;
    private final Context context;
    private final OnScheduleUpdated callback;

    public ScheduleListAdapter(Context context, List<FocusSchedule> schedules, OnScheduleUpdated callback) {
        this.context = context;
        this.schedules = schedules;
        this.callback = callback;
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView scheduleText;
        TextView dayName;
        ImageButton editButton;
        ImageButton deleteButton;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleText = itemView.findViewById(R.id.schedule_text);
            dayName = itemView.findViewById(R.id.schedule_day);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.schedule_list_item, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        final FocusSchedule schedule = schedules.get(position);

        String day = getDayName(schedule.getDayOfWeek());
        String time = formatTime(schedule.getStartHour(), schedule.getStartMinute()) +
                " - " +
                formatTime(schedule.getEndHour(), schedule.getEndMinute());

        holder.dayName.setText(day);
        holder.scheduleText.setText(time);

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onEdit(position);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Sunday";
            case 2: return "Monday";
            case 3: return "Tuesday";
            case 4: return "Wednesday";
            case 5: return "Thursday";
            case 6: return "Friday";
            case 7: return "Saturday";
            default: return "Unknown";
        }
    }

    private String formatTime(int hour, int minute) {
        boolean isPM = hour >= 12;
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = isPM ? "PM" : "AM";
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }
}
