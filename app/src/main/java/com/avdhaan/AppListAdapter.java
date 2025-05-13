package com.avdhaan;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private final List<AppInfo> appList;
    private final Set<String> blockedPackages = new HashSet<>();
    private final Consumer<Set<String>> onBlockedChanged;

    public AppListAdapter(List<AppInfo> apps, Consumer<Set<String>> onBlockedChanged) {
        this.appList = apps;
        this.onBlockedChanged = onBlockedChanged;

        for (AppInfo app : apps) {
            if (app.isBlocked) {
                blockedPackages.add(app.packageName);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.app_icon);
            name = view.findViewById(R.id.app_name);
            checkBox = view.findViewById(R.id.app_checkbox);
        }
    }

    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo app = appList.get(position);

        holder.icon.setImageDrawable(app.icon);
        holder.name.setText(app.name);

        // 🚫 Prevent triggering old listeners
        holder.checkBox.setOnCheckedChangeListener(null);

        // ✅ Set checkbox state
        holder.checkBox.setChecked(app.isBlocked);

        // ✅ Now set listener
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.isBlocked = isChecked;
            if (isChecked) {
                blockedPackages.add(app.packageName);
            } else {
                blockedPackages.remove(app.packageName);
            }
            onBlockedChanged.accept(blockedPackages);
        });
    }


    @Override
    public int getItemCount() {
        return appList.size();
    }
}
