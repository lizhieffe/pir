package com.android_things.sensor_experiment.controllers;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorData;
import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorListener;
import com.android_things.sensor_experiment.drivers.tcs_34725.Color;
import com.android_things.sensor_experiment.drivers.tcs_34725.Tcs34725SensorDriver;
import com.android_things.sensor_experiment.drivers.tcs_34725.Tcs34725SensorListener;
import com.android_things.sensor_experiment.pir.sensor_test.R;

/**
 * Created by lizhi on 1/24/18.
 */

public class MainUiController implements Pms7003SensorListener, Tcs34725SensorListener {
    private Activity mActivity;

    // Reading inside the delay will not be shown on the screen.
    private final static long DISPLAY_DELAY_MS = 500;
    private long mLastDisplayUpdatePms7003Ms;
    private long mLastDisplayUpdateTcs34725Ms;

    private TextView mPms7003View;
    private TextView mTcs34725View;

    public MainUiController(Activity activity) {
        mActivity = activity;

        mPms7003View = mActivity.findViewById(R.id.air_quality_text_view);
        mTcs34725View = mActivity.findViewById(R.id.rgb_text_view);
    }

    @Override
    public void onPms7003SensorData(Pms7003SensorData data) {
        final Pms7003SensorData localData = data;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastDisplayUpdatePms7003Ms > DISPLAY_DELAY_MS) {
                    mPms7003View.setText(localData.toString());
                    mLastDisplayUpdatePms7003Ms = currTimeMs;
                }
            }
        });
    }

    @Override
    public void onTcs34725SensorData(Color color) {
        final Color localData = color;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastDisplayUpdateTcs34725Ms > DISPLAY_DELAY_MS) {
                    mTcs34725View.setText(localData.toString());
                    mLastDisplayUpdateTcs34725Ms = currTimeMs;
                }
            }
        });
    }
}
