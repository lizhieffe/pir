package com.example.android_things.pir.myapplication;

import com.google.android.things.pio.Gpio;

interface MotionSensor {

    void startup();

    void shutdown();

    interface Listener {
        void onMovement(Gpio gpio);
    }

}