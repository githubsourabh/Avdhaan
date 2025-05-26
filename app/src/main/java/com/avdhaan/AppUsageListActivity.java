package com.avdhaan;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    private TextView emptyView;
    private final ExecutorService executor = AppDatabase.databaseWriteExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_list);

        recyclerView = findViewById(R.id.recyclerViewAppUsage);
        emptyView = findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsageLogs();
    }

    private void loadUsageLogs() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                List<AppUsage> logs = db.appUsageDao().getRecentLogs(100);
                Log.d("AppUsageList", "Logs found: " + logs.size());

                if (!isFinishing()) {
                    runOnUiThread(() -> {
                        if (logs.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            AppUsageAdapter adapter = new AppUsageAdapter(this, logs);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("AppUsageList", "Error loading usage logs", e);
                if (!isFinishing()) {
                    runOnUiThread(() -> {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't shutdown the executor as it's shared across the app
    }
}
