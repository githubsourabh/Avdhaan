package com.avdhaan;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avdhaan.db.AppUsageSummary;
import com.avdhaan.viewmodel.DashboardViewModel;

import java.util.ArrayList;

public class AppUsageListActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private AppUsageSummaryAdapter adapter;
    private Spinner timeRangeSpinner;
    private Switch blockedToggle;
    private TextView emptyView;
    private RecyclerView usageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_list);

        usageList = findViewById(R.id.recycler_usage_stats);
        usageList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppUsageSummaryAdapter(new ArrayList<>(), getPackageManager());
        usageList.setAdapter(adapter);

        emptyView = findViewById(R.id.emptyView);
        timeRangeSpinner = findViewById(R.id.spinner_time_range);
        blockedToggle = findViewById(R.id.switch_focus_blocked_only);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        viewModel.getUsageSummaryLiveData().observe(this, summaries -> {
            adapter.updateData(summaries);
            if (summaries == null || summaries.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                usageList.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                usageList.setVisibility(View.VISIBLE);
            }
        });

        // Spinner selection changes
        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                loadSummaryFromFilters();
            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Toggle changes
        blockedToggle.setOnCheckedChangeListener((buttonView, isChecked) -> loadSummaryFromFilters());

        // Initial load (Today, all apps)
        loadSummaryFromFilters();
    }

    private void loadSummaryFromFilters() {
        int daysBack = timeRangeSpinner.getSelectedItemPosition() == 1 ? 7 : 0;  // 0 = Today, 1 = 7 days
        boolean onlyBlocked = blockedToggle.isChecked();
        viewModel.loadUsageSummary(daysBack, onlyBlocked);
    }
}