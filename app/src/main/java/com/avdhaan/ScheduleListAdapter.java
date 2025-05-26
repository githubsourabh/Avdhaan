package com.avdhaan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.avdhaan.db.FocusSchedule;

public class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ScheduleViewHolder> {

    private static final Map<Integer, String> DAY_NAMES = new HashMap<>();
    static {
        DAY_NAMES.put(1, "Sunday");
        DAY_NAMES.put(2, "Monday");
        DAY_NAMES.put(3, "Tuesday");
        DAY_NAMES.put(4, "Wednesday");
        DAY_NAMES.put(5, "Thursday");
        DAY_NAMES.put(6, "Friday");
        DAY_NAMES.put(7, "Saturday");
    }

    public interface OnScheduleUpdated {
        void onEdit(int position);
        void onDelete(int position);
        void onScheduleUpdated();
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
        FocusSchedule schedule = schedules.get(position);
        String day = getDayName(schedule.getDayOfWeek());
        String time = formatTime(schedule.getStartHour(), schedule.getStartMinute()) +
                " - " +
                formatTime(schedule.getEndHour(), schedule.getEndMinute());
        holder.scheduleText.setText(day + " " + time);

        holder.editButton.setOnClickListener(v -> callback.onEdit(position));
        holder.deleteButton.setOnClickListener(v -> callback.onDelete(position));

        holder.itemView.setOnClickListener(v -> {
            // Handle edit click
            callback.onScheduleUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    private String getDayName(int dayOfWeek) {
        return DAY_NAMES.getOrDefault(dayOfWeek, "Unknown");
    }

    private String formatTime(int hour, int minute) {
        boolean isPM = hour >= 12;
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = isPM ? "PM" : "AM";
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }

    public void updateSchedules(List<FocusSchedule> newSchedules) {
        this.schedules.clear();
        this.schedules.addAll(newSchedules);
        notifyDataSetChanged();
    }
}
