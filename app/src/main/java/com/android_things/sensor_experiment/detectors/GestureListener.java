package com.android_things.sensor_experiment.detectors;

import com.android_things.sensor_experiment.sensors.zx_gesture.ZxGestureSensor;

/**
 * Created by lizhieffe on 1/2/18.
 */

public interface GestureListener {
    public void onGesture(ZxGestureSensor.Gesture gesture);
}
