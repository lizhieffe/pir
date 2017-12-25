package com.android_things.sensor_experiment;

import com.google.android.things.pio.Gpio;

interface MotionSensor {

    void startup();

    void shutdown();

    interface Listener {
        void onMovement(Gpio gpio);
    }

}