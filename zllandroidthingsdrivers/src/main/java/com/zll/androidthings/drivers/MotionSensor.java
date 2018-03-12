package com.zll.androidthings.drivers;

import com.google.android.things.pio.Gpio;

import java.io.IOException;

/**
 * Created by lizhieffe on 3/11/18.
 */

public interface MotionSensor {
    void startup() throws IOException;

    void shutdown();

    interface Listener {
        void onMovement(Gpio gpio);
    }
}
