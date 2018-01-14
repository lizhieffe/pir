package com.android_things.sensor_experiment.logger;

import android.content.Context;
import android.util.Log;

import com.android_things.sensor_experiment.detectors.MotionDetectionEvent;
import com.android_things.sensor_experiment.detectors.MotionDetectorListener;
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
 * Created by lizhieffe on 12/24/17.
 */

public class MotionLogger implements MotionDetectorListener {
    private final static int WRITE_EVERY_N_SECONDS = 20;

    private Context mContext;
    private List<MotionData> mPirData;
    private long lastFileSaveTimeMs;

    private class MotionData {
        MotionData(long unixTimeMs) {
            mUnixTimeMs = unixTimeMs;
        }

        // Timestamp when the motion is detected.
        long mUnixTimeMs;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(mUnixTimeMs);
            return sb.toString();
        }
    }

    public MotionLogger(Context context) {
        mContext = context;
        mPirData = new ArrayList<>();
    }

    @Override
    synchronized public void onDetected(MotionDetectionEvent event) {
        long currTimeMs = System.currentTimeMillis();

        // Initialization for the first time.
        if (lastFileSaveTimeMs == 0) {
            lastFileSaveTimeMs = currTimeMs;
        }

        mPirData.add(new MotionData(currTimeMs));

        if (currTimeMs - lastFileSaveTimeMs > WRITE_EVERY_N_SECONDS * 1000) {
            lastFileSaveTimeMs = currTimeMs;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                String dataFileName = sdf.format(new Date(currTimeMs));
                // Although the data files are named by date, it may contains data on the near
                // boundary of the nearby date.
                File dataFile = FileSystemUtil.getOrCreatePirSensorDataFile(mContext, dataFileName);
                FileOutputStream fos = new FileOutputStream(dataFile, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                for (MotionData d : mPirData) {
                    bw.write(d.toString());
                    bw.newLine();
                }

                bw.close();
                Log.i("===lizhi", "recorded data to disk: " + dataFile.toPath());
            } catch (Exception e) {
                Log.e("===lizhi", "Cannot write pir data: " + e);
            } finally {
                mPirData.clear();
            }
        }
    }
}
