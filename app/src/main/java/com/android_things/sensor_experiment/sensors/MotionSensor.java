package com.android_things.sensor_experiment.sensors;

import com.google.android.things.pio.Gpio;

public interface MotionSensor {

    void startup();

    void shutdown();

    interface Listener {
        void onMovement(Gpio gpio);
    }

}