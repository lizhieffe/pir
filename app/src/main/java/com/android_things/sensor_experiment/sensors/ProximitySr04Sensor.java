package com.android_things.sensor_experiment.sensors;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 12/24/17.
 */

// Driver for the HC-SR04 proximity sensor.
public class ProximitySr04Sensor implements MotionSensor {
    private static final String ECHO_PIN = "BCM20";
    private static final String TRIGGER_PIN = "BCM21";

    private Gpio mEchoGpio;
    private Gpio mTriggerGpio;

    @Override
    public void startup() {
        PeripheralManagerService service = new PeripheralManagerService();

        Log.d(TAG, "Available GPIOS: " + service.getGpioList());

        try {
            mEchoGpio = service.openGpio(ECHO_PIN);
            mEchoGpio.setDirection(Gpio.DIRECTION_IN);
            mEchoGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mEchoGpio.setActiveType(Gpio.ACTIVE_HIGH);

            mTriggerGpio = service.openGpio(TRIGGER_PIN);
            mTriggerGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.d(TAG, "ProximitySr04Sensor.startup: Error on PeripheralIO API", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            mEchoGpio.close();
            mTriggerGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot close GPIO bus: ", e);
        }
    }

    // Returned value unit is cm.
    public double readDistanceSync() throws IOException, InterruptedException {
        if (mTriggerGpio == null || mEchoGpio == null) {
            return -1;
        }

        // Just to be sure, set the trigger first to false
        mTriggerGpio.setValue(false);
        Thread.sleep(0, 2000);  // 2000 nano seconds

        // Hold the trigger pin HIGH for at least 10 us
        mTriggerGpio.setValue(true);
        Thread.sleep(0,10000); //10 microsec

        mTriggerGpio.setValue(false);  // reset the trigger

        while (mEchoGpio.getValue() == false);
        long echo_start = System.nanoTime();

        while (mEchoGpio.getValue() == true);
        long echo_stop = System.nanoTime();

        // Calculate distance in centimeters. The constants
        // are coming from the datasheet, and calculated from the assumed speed
        // of sound in air at sea level (~340 m/s).
        return ((echo_stop - echo_start) / 1000.0 ) / 58.23 ; //cm
    }
}
