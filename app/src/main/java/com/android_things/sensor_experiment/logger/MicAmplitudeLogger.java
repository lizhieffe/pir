package com.android_things.sensor_experiment.logger;

import android.content.Context;
import android.util.Log;

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
 * Created by lizhieffe on 1/16/18.
 */

public class MicAmplitudeLogger {
    private final static int WRITE_EVERY_N_SECONDS = 20;
    private final static String DATA_FILE_NAME_PREFIX = "mic_amplitude_";

    private Context mContext;
    private List<AmplitudeData> mData;
    private long lastFileSaveTimeMs;

    private class AmplitudeData {
        AmplitudeData(long timeMs, int amplitude) {
            mTimeMs = timeMs;
            mAmplitude = amplitude;
        }
        long mTimeMs;
        int mAmplitude;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(mTimeMs);
            sb.append(" ");
            sb.append(mAmplitude);
            return sb.toString();
        }
    }

    public MicAmplitudeLogger(Context context) {
        mContext = context;
        mData = new ArrayList<>();
    }

    synchronized public void onData(int amplitude) {
        long currTimeMs = System.currentTimeMillis();

        // Initialization for the first time.
        if (lastFileSaveTimeMs == 0) {
            lastFileSaveTimeMs = currTimeMs;
        }

        mData.add(new AmplitudeData(currTimeMs, amplitude));

        if (currTimeMs - lastFileSaveTimeMs > WRITE_EVERY_N_SECONDS * 1000) {
            lastFileSaveTimeMs = currTimeMs;

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                String dataFileName = DATA_FILE_NAME_PREFIX
                        +sdf.format(new Date(currTimeMs));
                // Although the data files are named by date, it may contains data on the near
                // boundary of the nearby date.
                File dataFile = FileSystemUtil.getOrCreatePirSensorDataFile(mContext, dataFileName);
                FileOutputStream fos = new FileOutputStream(dataFile, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                for (AmplitudeData d : mData) {
                    bw.write(d.toString());
                    bw.newLine();
                }

                bw.close();
                Log.i("===lizhi", "recorded data to disk: " + dataFile.toPath());
            } catch (Exception e) {
                Log.e("===lizhi", "Cannot write pir data: " + e);
            } finally {
                mData.clear();
            }
        }
    }
}
