package com.android_things.sensor_experiment.drivers;

import com.google.android.things.pio.Gpio;

import java.io.IOException;

public interface MotionSensor {

    void startup() throws IOException;

    void shutdown();

    interface Listener {
        void onMovement(Gpio gpio);
    }

}