package com.android_things.sensor_experiment.logger;

import android.content.Context;
import android.util.Log;

import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorListener;
import com.android_things.sensor_experiment.utils.FileSystemUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by lizhieffe on 1/13/18.
 */

public class Mpu6500SensorLogger implements Mpu6500SensorListener {
    private final static int WRITE_EVERY_N_SECONDS = 60;
    private final static String ACCEL_DATA_FILE_NAME_PREFIX = "mpu_6500_accel_";
    private final static String GYRO_DATA_FILE_NAME_PREFIX = "mpu_6500_gyro_";

    private Context mContext;

    private List<DataHolder> mAccelData = new ArrayList<>();
    private List<DataHolder> mGyroData = new ArrayList<>();

    private TimeHolder mLastAccelFileSaveTimeMs = new TimeHolder();
    private TimeHolder mLastGyroFileSaveTimeMs = new TimeHolder();

    private class TimeHolder {
        long mTimeMs;
    }

    private class DataHolder {
        DataHolder(long timeMs, float[] data) {
            mTimeMs = timeMs;
            mData = data;
        }

        long mTimeMs;
        float[] mData;
    }

    public Mpu6500SensorLogger(Context context) {
        mContext = context;
    }

    @Override
    synchronized public void onAccelData(float[] data) {
        onData(data, mAccelData,
                mLastAccelFileSaveTimeMs, ACCEL_DATA_FILE_NAME_PREFIX);
    }

    @Override
    synchronized public void onGyroData(float[] data) {
        onData(data, mGyroData,
               mLastGyroFileSaveTimeMs, GYRO_DATA_FILE_NAME_PREFIX);
    }

    private void onData(float[] data, List<DataHolder> dataCache,
                        TimeHolder lastDiskSaveTime, String fileNamePrefix) {
        long currTimeMs = System.currentTimeMillis();
        dataCache.add(new DataHolder(currTimeMs, data));

        // Initialization for the first time.
        if (lastDiskSaveTime.mTimeMs == 0) {
            lastDiskSaveTime.mTimeMs = currTimeMs;
        }

        if (currTimeMs - lastDiskSaveTime.mTimeMs > WRITE_EVERY_N_SECONDS * 1000) {
            saveToDisk(dataCache, currTimeMs, fileNamePrefix);
            lastDiskSaveTime.mTimeMs = currTimeMs;
            dataCache.clear();
        }

    }

    private void saveToDisk(List<DataHolder> data, long currTimeMs,
                            String fileNamePrefix) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            String dataFileName = fileNamePrefix +
                    sdf.format(new Date(currTimeMs));
            // Although the data files are named by date, it may contains data on the near
            // boundary of the nearby date.
            File dataFile = FileSystemUtil.getOrCreatePirSensorDataFile(mContext, dataFileName);
            FileOutputStream fos = new FileOutputStream(dataFile, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (DataHolder d : data) {
                bw.write(Long.toString(d.mTimeMs));
                bw.write(" ");
                for (int i = 0; i < d.mData.length; i++) {
                    bw.write(Float.toString(d.mData[i]));
                    if (i < d.mData.length - 1) {
                        bw.write(" ");
                    }
                }
                bw.newLine();
            }

            bw.close();
            Log.i("===lizhi", "recorded data to disk: " + dataFile.toPath());
        } catch (Exception e) {
            Log.e("===lizhi", "Cannot write pir data: " + e);
        }
    }
}
