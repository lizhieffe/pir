package com.android_things.sensor_experiment.motion;

import com.android_things.sensor_experiment.sensors.ProximitySr04Sensor;

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

    @Override
    public String toString() {
        if (mSource == Source.PIR) {
           return "[source]PIR";
        } else if (mSource == Source.PROXIMITY) {
            return "[source]PROXIMITY, [distance]" + mProxmityParam;
        } else {
            return "toString() not implemented yet for source: " + mSource;
        }
    }
}
