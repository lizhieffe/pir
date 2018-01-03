package com.android_things.sensor_experiment.indicator;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.detectors.GestureListener;
import com.android_things.sensor_experiment.sensors.zx_gesture.ZxGestureSensor;


/**
 * Created by lizhieffe on 1/2/18.
 */

public class GestureIndicator implements DetectionIndicator, GestureListener {
    private TextView mGestureView;

    public GestureIndicator(TextView gestureView) {
        mGestureView = gestureView;
    }

    @Override
    public void start() {

    }

    @Override
    public void close() {

    }

    @Override
    public void onGesture(final ZxGestureSensor.Gesture gesture) {
        if (gesture != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mGestureView.setText(gesture.toString());
                }
            });
        }
    }
}
