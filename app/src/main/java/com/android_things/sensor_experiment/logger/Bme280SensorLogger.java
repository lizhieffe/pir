package com.android_things.sensor_experiment.logger;

import android.content.Context;
import android.util.Log;

import com.android_things.sensor_experiment.drivers.bme_280_sensor.Bme280SensorListener;
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
 * Created by lizhieffe on 1/20/18.
 */

public class Bme280SensorLogger implements Bme280SensorListener {
    private final static int WRITE_EVERY_N_SECONDS = 60;
    private final static String TEMPERATURE_DATA_FILE_NAME_PREFIX = "bme_280_temperature_";
    private final static String PRESSURE_DATA_FILE_NAME_PREFIX = "bme_280_pressure_";
    private final static String HUMIDITY_DATA_FILE_NAME_PREFIX = "bme_280_humidity_";

    private Context mContext;

    private class DataHolder {
        DataHolder(long timeMs, float data) {
            mTimeMs = timeMs;
            mData = data;
        }
        long mTimeMs;
        float mData;
    }

    private TimeHolder mLastTemperatureSaveTimeMs = new TimeHolder();
    private TimeHolder mLastPressureSaveTimeMs = new TimeHolder();
    private TimeHolder mLastHumiditySaveTimeMs = new TimeHolder();

    private class TimeHolder {
        long mTimeMs;
    }

    private List<DataHolder> mTemperatureData;
    private List<DataHolder> mPressureData;
    private List<DataHolder> mHumidityData;

    public Bme280SensorLogger(Context context) {
        mContext = context;

        mTemperatureData = new ArrayList<>();
        mPressureData = new ArrayList<>();
        mHumidityData = new ArrayList<>();
    }

    @Override
    synchronized public void onTemperatureData(float data) {
        onData(data, mTemperatureData, mLastTemperatureSaveTimeMs,
                TEMPERATURE_DATA_FILE_NAME_PREFIX);
    }

    @Override
    synchronized public void onPressureData(float data) {
        onData(data, mPressureData, mLastPressureSaveTimeMs,
                PRESSURE_DATA_FILE_NAME_PREFIX);
    }

    @Override
    synchronized public void onHumidityData(float data) {
        onData(data, mHumidityData, mLastHumiditySaveTimeMs,
                HUMIDITY_DATA_FILE_NAME_PREFIX);
    }

    private void onData(float data, List<DataHolder> dataCache,
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
                bw.write(Float.toString(d.mData));
                bw.newLine();
            }

            bw.close();
            Log.i("===lizhi", "recorded data to disk: " + dataFile.toPath());
        } catch (Exception e) {
            Log.e("===lizhi", "Cannot write pir data: " + e);
        }
    }
}
