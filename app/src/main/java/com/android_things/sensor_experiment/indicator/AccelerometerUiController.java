package com.android_things.sensor_experiment.indicator;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorListener;

/**
 * Created by lizhi on 1/12/18.
 */

public class AccelerometerUiController implements Mpu6500SensorListener {
    private TextView mAccelView;
    private TextView mGyroView;

    // Reading inside the deplay will not be shown on the screen.
    private final static long DISPLAY_DELAY_MS = 500;
    private long mLastAccelDisplayUpldate;
    private long mLastGyroDisplayUpldate;

    public AccelerometerUiController(TextView accelView, TextView gyroView) {
        mAccelView = accelView;
        mGyroView = gyroView;
    }

    @Override
    public void onAccelData(float[] data) {
        final float[] finalData = data;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastAccelDisplayUpldate > DISPLAY_DELAY_MS) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("Accel read [x, y, z]: ");
                    sb.append(String.format("%.4f", finalData[0]));
                    sb.append(", ");
                    sb.append(String.format("%.4f", finalData[1]));
                    sb.append(", ");
                    sb.append(String.format("%.4f", finalData[2]));

                    mAccelView.setText(sb.toString());
                    mLastAccelDisplayUpldate = currTimeMs;
                }
            }
        });
    }

    @Override
    public void onGyroData(float[] data) {
        final float[] finalData = data;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastGyroDisplayUpldate > DISPLAY_DELAY_MS) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("Gyro read [x, y, z]: ");
                    sb.append(String.format("%.4f", finalData[0]));
                    sb.append(", ");
                    sb.append(String.format("%.4f", finalData[1]));
                    sb.append(", ");
                    sb.append(String.format("%.4f\n", finalData[2]));

                    mGyroView.setText(sb.toString());
                    mLastGyroDisplayUpldate = currTimeMs;
                }
            }
        });
    }
}
