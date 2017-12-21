package com.example.android_things.pir.myapplication;

interface MotionSensor {

    void startup();

    void shutdown();

    interface Listener {
        void onMovement();
    }

}