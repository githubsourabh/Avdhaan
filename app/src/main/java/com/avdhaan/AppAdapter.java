package com.avdhaan;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
    private static final String TAG = "AppAdapter";
    private List<AppInfo> appList;

    public AppAdapter(List<AppInfo> appList) {
        this.appList = appList;
        Log.d(TAG, "Adapter created with " + appList.size() + " apps");
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(app.getIcon());
        holder.checkBox.setChecked(app.isSelected());

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !app.isSelected();
            app.setSelected(newState);
            holder.checkBox.setChecked(newState);
            Log.d(TAG, "App " + app.getAppName() + " selected: " + newState);
        });

        holder.checkBox.setOnClickListener(v -> {
            app.setSelected(holder.checkBox.isChecked());
            Log.d(TAG, "App " + app.getAppName() + " selected: " + holder.checkBox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        CheckBox checkBox;

        AppViewHolder(View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            checkBox = itemView.findViewById(R.id.checkbox_app);
        }
    }
} 