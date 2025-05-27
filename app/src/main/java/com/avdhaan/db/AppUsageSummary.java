package com.avdhaan.db;

public class AppUsageSummary {
    public String packageName;
    public long totalUsage;      // in milliseconds
    public int totalAttempts;

    public AppUsageSummary(String packageName, long totalUsage, int totalAttempts) {
        this.packageName = packageName;
        this.totalUsage = totalUsage;
        this.totalAttempts = totalAttempts;
    }
}