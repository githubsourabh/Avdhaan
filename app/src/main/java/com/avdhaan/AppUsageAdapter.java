
package com.avdhaan;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avdhaan.db.AppUsage;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppUsageAdapter extends RecyclerView.Adapter<AppUsageAdapter.ViewHolder> {

    private final Context context;
    private final List<AppUsage> usageList;

    public AppUsageAdapter(Context context, List<AppUsage> usageList) {
        this.context = context;
        this.usageList = usageList;
    }

    @NonNull
    @Override
    public AppUsageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_usage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppUsageAdapter.ViewHolder holder, int position) {
        AppUsage usage = usageList.get(position);

        holder.appName.setText(usage.packageName);
        holder.usageDuration.setText(formatDuration(usage.usageTimeMillis));

        try {
            Drawable icon = context.getPackageManager()
                    .getApplicationIcon(usage.packageName);
            holder.appIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            holder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    @Override
    public int getItemCount() {
        return usageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView usageDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.imageViewAppIcon);
            appName = itemView.findViewById(R.id.textViewAppName);
            usageDuration = itemView.findViewById(R.id.textViewDuration);
        }
    }

    private String formatDuration(long durationMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;
        return String.format("%d min %02d sec", minutes, seconds);
    }
}
