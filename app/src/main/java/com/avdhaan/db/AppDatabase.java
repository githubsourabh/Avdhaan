package com.avdhaan.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.avdhaan.db.AppUsage;
import com.avdhaan.db.BlockedApp;
import com.avdhaan.db.BlockedAppGroup;
import com.avdhaan.db.FocusSchedule;

@Database(entities = {
        AppUsage.class,
        BlockedApp.class,
        BlockedAppGroup.class,
        FocusSchedule.class
}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "avdhaan_db";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract AppUsageDao appUsageDao();

    public abstract BlockedAppDao blockedAppDao();

    public abstract BlockedAppGroupDao blockedAppGroupDao();

    public abstract FocusScheduleDao focusScheduleDao();
}