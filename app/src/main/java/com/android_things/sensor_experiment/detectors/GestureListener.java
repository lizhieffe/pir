package com.android_things.sensor_experiment.detectors;

import com.zll.androidthings.drivers.zx_gesture_sensor.ZxGestureSensor;

/**
 * Created by lizhieffe on 1/2/18.
 */

public interface GestureListener {
    public void onGesture(ZxGestureSensor.Gesture gesture);
}
