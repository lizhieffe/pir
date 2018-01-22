package com.android_things.sensor_experiment.controllers;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.drivers.tcs_34725.Color;
import com.android_things.sensor_experiment.drivers.tcs_34725.Tcs34725SensorListener;

/**
 * Created by lizhieffe on 1/21/18.
 */

public class RgbUiController implements Tcs34725SensorListener {
    private TextView mView;

    // Reading inside the delay will not be shown on the screen.
    private final static long DISPLAY_DELAY_MS = 500;
    private long mLastDisplayUpdateMs;

    public RgbUiController(TextView view) {
        mView = view;
    }

    @Override
    public void onColorData(Color color) {
        final Color localColor = color;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastDisplayUpdateMs > DISPLAY_DELAY_MS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[RED]");
                    sb.append(String.format("%d", localColor.red));
                    sb.append(" ");
                    sb.append("[GREEN]");
                    sb.append(String.format("%d", localColor.green));
                    sb.append(" ");
                    sb.append("[BLUE]");
                    sb.append(String.format("%d", localColor.blue));
                    sb.append(" ");
                    sb.append("[CLEAR]");
                    sb.append(String.format("%d", localColor.clear));

                    mView.setText(sb.toString());
                    mLastDisplayUpdateMs = currTimeMs;
                }
            }
        });
    }
}
