package com.avdhaan;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avdhaan.db.AppUsage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppUsageAdapter extends RecyclerView.Adapter<AppUsageAdapter.ViewHolder> {

    private static final String TAG = "AppUsageAdapter";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Context context;
    private final List<AppUsage> usageList;
    private final PackageManager packageManager;
    private final Map<String, String> appNameCache = new HashMap<>();
    private final Map<String, Drawable> appIconCache = new HashMap<>();

    public AppUsageAdapter(Context context, List<AppUsage> usageList) {
        this.context = context;
        this.usageList = usageList;
        this.packageManager = context.getPackageManager();
        prefetchAppInfo();
    }

    private void prefetchAppInfo() {
        executor.execute(() -> {
            for (AppUsage usage : usageList) {
                if (!appNameCache.containsKey(usage.packageName)) {
                    try {
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(usage.packageName, 0);
                        String appName = packageManager.getApplicationLabel(appInfo).toString();
                        Drawable icon = packageManager.getApplicationIcon(appInfo);
                        
                        appNameCache.put(usage.packageName, appName);
                        appIconCache.put(usage.packageName, icon);
                        
                        // Notify adapter that item data has changed
                        mainHandler.post(() -> notifyDataSetChanged());
                    } catch (PackageManager.NameNotFoundException e) {
                        appNameCache.put(usage.packageName, usage.packageName);
                        appIconCache.put(usage.packageName, context.getDrawable(android.R.drawable.sym_def_app_icon));
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_usage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUsage usage = usageList.get(position);
        String appName = appNameCache.getOrDefault(usage.packageName, usage.packageName);
        Drawable icon = appIconCache.getOrDefault(usage.packageName, 
            context.getDrawable(android.R.drawable.sym_def_app_icon));

        holder.appName.setText(appName);
        holder.usageDuration.setText(formatDuration(usage.usageTimeMillis));
        holder.appIcon.setImageDrawable(icon);
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

    private static String formatDuration(long durationMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;
        return String.format("%d min %02d sec", minutes, seconds);
    }

    public void onDestroy() {
        executor.shutdown();
    }
}
