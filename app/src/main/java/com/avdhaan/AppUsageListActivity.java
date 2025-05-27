package com.avdhaan;

import android.content.Intent;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;

import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class AppUsageListActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private AppUsageSummaryAdapter adapter;
    private Spinner timeRangeSpinner;
    private Switch blockedToggle;
    private TextView emptyView;
    private RecyclerView usageList;
    private PieChart pieChart;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_list);

        // ✅ Setup toolbar with Home navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        usageList = findViewById(R.id.recycler_usage_stats);
        usageList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppUsageSummaryAdapter(new ArrayList<>(), getPackageManager());
        usageList.setAdapter(adapter);

        emptyView = findViewById(R.id.emptyView);
        timeRangeSpinner = findViewById(R.id.spinner_time_range);
        blockedToggle = findViewById(R.id.switch_focus_blocked_only);
        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        viewModel.getUsageSummaryLiveData().observe(this, summaries -> {
            boolean isEmpty = (summaries == null || summaries.isEmpty());
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            usageList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            adapter.updateData(summaries);
            updatePieChart(summaries);
            updateBarChart(summaries);
        });

        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                loadSummaryFromFilters();
            }

            public void onNothingSelected(AdapterView<?> parent) { }
        });

        blockedToggle.setOnCheckedChangeListener((buttonView, isChecked) -> loadSummaryFromFilters());

        loadSummaryFromFilters();
    }

    private void loadSummaryFromFilters() {
        int daysBack = timeRangeSpinner.getSelectedItemPosition() == 1 ? 7 : 0;
        boolean onlyBlocked = blockedToggle.isChecked();
        viewModel.loadUsageSummary(daysBack, onlyBlocked);
    }

    private void updatePieChart(List<AppUsageSummary> data) {
        List<PieEntry> entries = new ArrayList<>();
        long totalMillis = 0;

        for (AppUsageSummary item : data) {
            if (item.totalUsage > 0) {
                totalMillis += item.totalUsage;
                String label;
                try {
                    label = getPackageManager().getApplicationLabel(
                            getPackageManager().getApplicationInfo(item.packageName, 0)
                    ).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    label = item.packageName;
                }
                entries.add(new PieEntry(item.totalUsage, label));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Time Spent");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        long totalMinutes = totalMillis / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String centerText = hours > 0
                ? String.format("Total\n%dh %dm", hours, minutes)
                : String.format("Total\n%dm", minutes);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(14f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.invalidate();
    }

    private void updateBarChart(List<AppUsageSummary> data) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            AppUsageSummary item = data.get(i);
            entries.add(new BarEntry(i, item.totalAttempts));
            try {
                String label = getPackageManager().getApplicationLabel(
                        getPackageManager().getApplicationInfo(item.packageName, 0)
                ).toString();
                labels.add(label);
            } catch (PackageManager.NameNotFoundException e) {
                labels.add(item.packageName);
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Open Attempts");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setLabelRotationAngle(-45);
        barChart.setFitBars(true);
        barChart.invalidate();
    }
}