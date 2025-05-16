
package com.avdhaan;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.AppUsage;

import java.util.List;
import java.util.concurrent.Executors;

public class AppUsageListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_list);

        recyclerView = findViewById(R.id.recyclerViewAppUsage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<AppUsage> logs = AppDatabase.getInstance(this)
                    .appUsageDao()
                    .getRecentLogs(100);

            runOnUiThread(() -> {
                AppUsageAdapter adapter = new AppUsageAdapter(this, logs);
                recyclerView.setAdapter(adapter);
            });
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.appUsageDao().insert(new AppUsage("com.test.app", 540000, System.currentTimeMillis()));

            List<AppUsage> logs = db.appUsageDao().getRecentLogs(100);
            Log.d("AppUsageList", "Logs found: " + logs.size());

            runOnUiThread(() -> {
                AppUsageAdapter adapter = new AppUsageAdapter(this, logs);
                recyclerView.setAdapter(adapter);
            });
        });

    }
}
