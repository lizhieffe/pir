package com.android_things.sensor_experiment;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by lizhieffe on 12/24/17.
 */

public class FileSystemUtil {
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getOrCreateSensorDataStorageDir(Context context) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), "sensor_data");
        if (!file.exists() && !file.mkdirs()) {
            Log.e("===lizhi", "Directory not created");
        }
        return file;
    }

    public static File getOrCreatePirSensorDataFile(Context context) throws IOException {
        File file = new File(FileSystemUtil.getOrCreateSensorDataStorageDir(context),
                "pir_sensor_data.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
}
