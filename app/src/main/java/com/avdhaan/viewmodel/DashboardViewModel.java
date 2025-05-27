package com.avdhaan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.avdhaan.db.AppDatabase;
import com.avdhaan.db.AppUsageDao;
import com.avdhaan.db.AppUsageSummary;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends AndroidViewModel {

    private final AppUsageDao appUsageDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<AppUsageSummary>> usageSummaryLiveData = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        appUsageDao = AppDatabase.getInstance(application).appUsageDao();
    }

    public LiveData<List<AppUsageSummary>> getUsageSummaryLiveData() {
        return usageSummaryLiveData;
    }

    public void loadUsageSummary(int daysBack, boolean onlyBlockedDuringFocus) {
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysBack);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        executor.execute(() -> {
            List<AppUsageSummary> summary;
            if (onlyBlockedDuringFocus) {
                summary = appUsageDao.getBlockedFocusUsageSummary(startTime, now);
            } else {
                summary = appUsageDao.getUsageSummary(startTime, now);
            }
            usageSummaryLiveData.postValue(summary);
        });
    }
}