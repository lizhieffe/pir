package com.android_things.sensor_experiment.motion;

/**
 * Created by lizhieffe on 12/25/17.
 */

public class MotionDetectionEvent {
    enum Source {
        PIR,
        PROXIMITY
    }

    public Source mSource;

    // When the source is PROXIMITY, this field represents the distance to the
    // detected object.
    public double mProxmityParam;
}