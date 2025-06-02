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
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
    private static final String TAG = "AppAdapter";
    private static final int APP_TAG = 1;
    private List<AppInfo> appList;

    public AppAdapter(List<AppInfo> appList) {
        this.appList = appList;
        setHasStableIds(true);
        Log.d(TAG, "Adapter created with " + appList.size() + " apps");
    }

    @Override
    public long getItemId(int position) {
        return appList.get(position).getPackageName().hashCode();
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
        // Clear previous state
        holder.checkBox.setOnCheckedChangeListener(null);
        
        AppInfo app = appList.get(position);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(app.getIcon());
        holder.itemView.setTag(APP_TAG, app.getPackageName());
        
        // Set checkbox state after clearing listener
        holder.checkBox.setChecked(app.isSelected());

        // Set new listener
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.setSelected(isChecked);
            Log.d(TAG, "App " + app.getAppName() + " selected: " + isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !app.isSelected();
            app.setSelected(newState);
            holder.checkBox.setChecked(newState);
            Log.d(TAG, "App " + app.getAppName() + " selected: " + newState);
        });
    }

    @Override
    public void onViewRecycled(@NonNull AppViewHolder holder) {
        super.onViewRecycled(holder);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(false);
        holder.itemView.setTag(APP_TAG, null);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public void updateApps(List<AppInfo> newApps) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AppDiffCallback(appList, newApps));
        appList.clear();
        appList.addAll(newApps);
        diffResult.dispatchUpdatesTo(this);
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

    private static class AppDiffCallback extends DiffUtil.Callback {
        private final List<AppInfo> oldList;
        private final List<AppInfo> newList;

        AppDiffCallback(List<AppInfo> oldList, List<AppInfo> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getPackageName()
                    .equals(newList.get(newItemPosition).getPackageName());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            AppInfo oldApp = oldList.get(oldItemPosition);
            AppInfo newApp = newList.get(newItemPosition);
            return oldApp.isSelected() == newApp.isSelected() &&
                   oldApp.getAppName().equals(newApp.getAppName());
        }
    }
} 