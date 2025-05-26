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
import java.util.concurrent.ExecutorService;

public class AppUsageListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private final ExecutorService executor = AppDatabase.databaseWriteExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_list);

        recyclerView = findViewById(R.id.recyclerViewAppUsage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<AppUsage> logs = db.appUsageDao().getRecentLogs(100);
            Log.d("AppUsageList", "Logs found: " + logs.size());

            if (!isFinishing()) {
                runOnUiThread(() -> {
                    AppUsageAdapter adapter = new AppUsageAdapter(this, logs);
                    recyclerView.setAdapter(adapter);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't shutdown the executor as it's shared across the app
    }
}
