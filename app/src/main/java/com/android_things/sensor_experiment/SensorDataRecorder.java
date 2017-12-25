package com.android_things.sensor_experiment;

import android.content.Context;
import android.util.Log;

import com.google.android.things.pio.Gpio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

/**
 * Created by lizhieffe on 12/24/17.
 */

class SensorDataRecorder implements MotionSensor.Listener {
    private static int WRITE_EVERY_N_ITEM = 100;

    private Context mContext;
    private List<PirData> mPirData;

    private class PirData {
        PirData(long unixTimeMs, boolean state) {
            mUnixTimeMs = unixTimeMs;
            mState = state;
        }

        long mUnixTimeMs;
        boolean mState;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(mUnixTimeMs);
            sb.append(" ");
            sb.append(mState ? 1 : 0);
            return sb.toString();
        }
    }

    public SensorDataRecorder(Context context) {
        mContext = context;
        mPirData = new ArrayList<>(WRITE_EVERY_N_ITEM);
    }

    @Override
    public void onMovement(Gpio gpio) {
        try {
            mPirData.add(new PirData(currentTimeMillis(), gpio.getValue()));
            Log.i("===lizhi", "recording data to memory");
        } catch(Exception e) {
            Log.e("===lizhi", "Cannot record pir data: " + e);
        }

        if (mPirData.size() == WRITE_EVERY_N_ITEM) {
            try {
                File data_file = FileSystemUtil.getOrCreatePirSensorDataFile(mContext);
                data_file.delete();
                data_file = FileSystemUtil.getOrCreatePirSensorDataFile(mContext);
                FileOutputStream fos = new FileOutputStream(data_file);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                for (PirData d : mPirData) {
                    bw.write(d.toString());
                    bw.newLine();
                }

                bw.close();
                Log.i("===lizhi", "recorded data to disk: " + data_file.toPath());
            } catch (Exception e) {
                Log.e("===lizhi", "Cannot write pir data: " + e);
            } finally {
                mPirData.clear();
            }
        }
    }
}