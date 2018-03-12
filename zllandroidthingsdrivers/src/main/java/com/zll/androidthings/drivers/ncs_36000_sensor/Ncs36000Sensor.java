package com.zll.androidthings.drivers.ncs_36000_sensor;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.zll.androidthings.drivers.MotionSensor;

import java.io.IOException;

/**
 * Created by lizhieffe on 1/25/18.
 *
 * Driver for NCS-36000 PIR sensor produced by Sparkfun.
 */

public class Ncs36000Sensor implements MotionSensor {
    private static final String TAG = "NCS-36000 Sensor";

    // Default GPIO to connect to the OUT pin for the sensor.
    private static final String DEFAULT_GPIO_PIN = "BCM4";

    private String mGpioPin;
    private Gpio mBus;

    private volatile boolean mIsMotionDetected;

    Ncs36000Sensor() {
        this(DEFAULT_GPIO_PIN);
    }

    Ncs36000Sensor(String gpioPin) {
        mGpioPin = gpioPin;
    }

    @Override
    public void startup() {
        Log.d(TAG, "Ncs36000Sensor.startup: starttt..");
        try {
            mBus = new PeripheralManagerService().openGpio(mGpioPin);
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
                Log.d(TAG, "Ncs36000Sensor.onGpioEdge: callback " + gpio.getValue());
            } catch (IOException e) {
                Log.e(TAG, "Ncs36000Sensor.onGpioEdge: ", e);
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
