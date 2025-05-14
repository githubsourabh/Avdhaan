
package com.avdhaan;

import android.content.Context;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStorage {
    private static final String FILE_NAME = "focus_schedules.dat";

    public static void saveSchedules(Context context, List<FocusSchedule> schedules) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(new File(context.getFilesDir(), FILE_NAME)))) {
            out.writeObject(schedules);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FocusSchedule> loadSchedules(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<FocusSchedule>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
