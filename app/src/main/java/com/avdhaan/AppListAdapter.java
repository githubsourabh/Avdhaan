package com.avdhaan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        // Preload initially blocked apps
        for (AppInfo app : apps) {
            if (app.isBlocked()) {
                blockedPackages.add(app.getPackageName());
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
            checkBox = itemView.findViewById(R.id.app_checkbox);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = appList.get(position);

        holder.icon.setImageDrawable(app.getIcon());
        holder.name.setText(app.getName());

        // Avoid recycling issues
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(blockedPackages.contains(app.getPackageName()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.setBlocked(isChecked);
            if (isChecked) {
                blockedPackages.add(app.getPackageName());
            } else {
                blockedPackages.remove(app.getPackageName());
            }
            onBlockedChanged.accept(new HashSet<>(blockedPackages));
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }
}
