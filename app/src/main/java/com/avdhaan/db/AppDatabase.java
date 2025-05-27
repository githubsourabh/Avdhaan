package com.avdhaan.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.avdhaan.db.AppUsage;
import com.avdhaan.db.BlockedApp;
import com.avdhaan.db.BlockedAppGroup;
import com.avdhaan.db.FocusSchedule;

@Database(entities = {
        AppUsage.class,
        BlockedApp.class,
        BlockedAppGroup.class,
        FocusSchedule.class
}, version = 3, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "avdhaan_db";
    private static AppDatabase instance;

    // Using 2 threads for database operations to ensure good performance
    // across all Android devices while maintaining reasonable resource usage
    private static final int NUMBER_OF_THREADS = 2;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create temporary table with new schema
            database.execSQL("CREATE TABLE IF NOT EXISTS focus_schedules_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "groupId INTEGER NOT NULL, " +
                    "dayOfWeek INTEGER NOT NULL, " +
                    "startHour INTEGER NOT NULL, " +
                    "startMinute INTEGER NOT NULL, " +
                    "endHour INTEGER NOT NULL, " +
                    "endMinute INTEGER NOT NULL)");

            // Copy data from old table to new table, converting time strings to hours and minutes
            database.execSQL("INSERT INTO focus_schedules_new (id, groupId, dayOfWeek, startHour, startMinute, endHour, endMinute) " +
                    "SELECT id, groupId, dayOfWeek, " +
                    "CAST(substr(startTime, 1, 2) AS INTEGER), " +
                    "CAST(substr(startTime, 4, 2) AS INTEGER), " +
                    "CAST(substr(endTime, 1, 2) AS INTEGER), " +
                    "CAST(substr(endTime, 4, 2) AS INTEGER) " +
                    "FROM focus_schedules");

            // Drop old table
            database.execSQL("DROP TABLE focus_schedules");

            // Rename new table to original name
            database.execSQL("ALTER TABLE focus_schedules_new RENAME TO focus_schedules");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DB_NAME)
                    .addMigrations(MIGRATION_2_3)
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
