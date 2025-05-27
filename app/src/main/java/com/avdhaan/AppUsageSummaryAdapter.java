package com.avdhaan;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avdhaan.db.AppUsageSummary;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppUsageSummaryAdapter extends RecyclerView.Adapter<AppUsageSummaryAdapter.ViewHolder> {

    private final List<AppUsageSummary> data;
    private final PackageManager packageManager;

    public AppUsageSummaryAdapter(List<AppUsageSummary> data, PackageManager packageManager) {
        this.data = data;
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_usage_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUsageSummary summary = data.get(position);
        String packageName = summary.packageName;

        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            holder.appName.setText(packageManager.getApplicationLabel(appInfo));
            holder.icon.setImageDrawable(packageManager.getApplicationIcon(appInfo));
        } catch (PackageManager.NameNotFoundException e) {
            holder.appName.setText(packageName);
            holder.icon.setImageDrawable(null);
        }

        long millis = summary.totalUsage;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        holder.usageTime.setText(String.format("%d min %d sec", minutes, seconds));

        holder.openAttempts.setText(String.format("Attempts: %d", summary.totalAttempts));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<AppUsageSummary> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView appName;
        TextView usageTime;
        TextView openAttempts;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            usageTime = itemView.findViewById(R.id.app_usage_time);
            openAttempts = itemView.findViewById(R.id.app_open_attempts);
        }
    }
}