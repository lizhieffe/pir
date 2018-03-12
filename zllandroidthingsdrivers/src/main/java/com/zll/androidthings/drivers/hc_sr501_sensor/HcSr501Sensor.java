package com.zll.androidthings.drivers.hc_sr501_sensor;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.zll.androidthings.drivers.MotionSensor;

import java.io.IOException;

/**
 * Created by lizhieffe on 1/2/18.
 *
 * HC-SR501 is a PIR montion sensor.
 */

public class HcSr501Sensor implements MotionSensor {
    private static final String TAG = "HC SR 501 Sensor";
    // The delay time starts to count once motion is detected. Within the delay
    // time, sensor always reports TRUE for detection. After delay time, sensor
    // reports FALSE. If another motion is detected within the delay time, the
    // delay time is reset.
    public final static long DELAY_TIME_MS = 5000;

    private Gpio mBus;

    private volatile boolean mIsMotionDetected;

    @Override
    public void startup() {
        try {
            // BCM4 is the GPIO pin I have the sensor connected to on my raspberry pi
            mBus = new PeripheralManagerService().openGpio("BCM4");
            // What direction we expect data to travel. This is a sensor so we expect it to send us
            // data. Therefore we will use Gpio.DIRECTION_IN.
            mBus.setDirection(Gpio.DIRECTION_IN);
            // GPIO is binary (true or false) so we are declaring what voltage signal equates to
            // true, a low or high voltage. For our sensor according to the datasheet a low voltage
            // means movement. Therefore we will use Gpio.ACTIVE_HIGH.
            mBus.setActiveType(Gpio.ACTIVE_HIGH);
            // This is what state change we want monitor, from high voltage to low, low to high or
            // both. Meaning we could listen for movement starting, ending or both. We want to
            // listen for movement starting. Therefore we will use Gpio.EDGE_RISING.
            mBus.setEdgeTriggerType(Gpio.EDGE_BOTH);
        } catch (IOException e) {
            throw new IllegalStateException("Sensor can't start - App is foobar'd", e);
        }
        try {
            mBus.registerGpioCallback(callback);
        } catch (IOException e) {
            throw new IllegalStateException("Sensor can't register callback - App is foobar'd", e);
        }
    }

    private final GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                mIsMotionDetected = gpio.getValue();
            } catch (IOException e) {
                Log.e(TAG, "HcSr501Sensor.onGpioEdge: ", e);
            }
            return true; // True to continue listening
        }
    };

    @Override
    public void shutdown() {
        mBus.unregisterGpioCallback(callback);
        try {
            mBus.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to shut down. You might get errors next time you try to start.", e);
        }
    }

    public boolean readData() {
        return mIsMotionDetected;
    }

}
