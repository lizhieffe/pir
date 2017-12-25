package com.android_things.sensor_experiment;

import android.content.Context;
import android.util.Log;

import com.android_things.sensor_experiment.motion.MotionDetectionListener;
import com.android_things.sensor_experiment.sensors.MotionSensor;
import com.android_things.sensor_experiment.utils.FileSystemUtil;
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

class SensorDataRecorder implements MotionDetectionListener {
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
    synchronized public void onDetected() {
        mPirData.add(new PirData(currentTimeMillis(), true));

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
