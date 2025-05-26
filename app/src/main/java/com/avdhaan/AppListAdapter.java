package com.avdhaan;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    public interface OnAppToggleListener {
        void onAppToggled(String packageName, boolean isChecked);
    }

    private final List<AppInfo> appList;
    private final Set<String> initiallyBlockedApps;
    private final OnAppToggleListener toggleListener;

    public AppListAdapter(List<AppInfo> appList, Set<String> blockedApps, OnAppToggleListener listener) {
        this.appList = appList;
        this.initiallyBlockedApps = blockedApps;
        this.toggleListener = listener;
    }

    @NonNull
    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppListAdapter.ViewHolder holder, int position) {
        AppInfo appInfo = appList.get(position);
        holder.appNameText.setText(appInfo.getAppName());
        holder.packageNameText.setText(appInfo.getPackageName());
        holder.appIcon.setImageDrawable(appInfo.getIcon());

        boolean isInitiallyChecked = initiallyBlockedApps.contains(appInfo.getPackageName());
        holder.blockCheckBox.setChecked(isInitiallyChecked);

        holder.blockCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleListener.onAppToggled(appInfo.getPackageName(), isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appNameText;
        TextView packageNameText;
        CheckBox blockCheckBox;

        public ViewHolder(View view) {
            super(view);
            appIcon = view.findViewById(R.id.app_icon);
            appNameText = view.findViewById(R.id.app_name);
            packageNameText = view.findViewById(R.id.textViewAppName);
            blockCheckBox = view.findViewById(R.id.checkbox_app);
        }
    }
}