package com.android_things.sensor_experiment.controllers;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.detectors.GestureListener;
import com.android_things.sensor_experiment.drivers.zx_gesture_sensor.ZxGestureSensor;


/**
 * Created by lizhieffe on 1/2/18.
 */

public class GestureController implements DetectionController, GestureListener {
    private TextView mGestureView;

    public GestureController(TextView gestureView) {
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
