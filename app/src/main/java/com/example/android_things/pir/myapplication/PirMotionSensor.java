package com.example.android_things.pir.myapplication;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;

import java.io.IOException;
import java.util.List;

/**
 * HC-SR501
 */
class PirMotionSensor implements MotionSensor {

    private final Gpio bus;

    private final List<Listener> listeners;

    PirMotionSensor(Gpio bus, List<Listener> listeners) {
        this.bus = bus;
        this.listeners = listeners;
    }

    @Override
    public void startup() {
        try {
            // What direction we expect data to travel. This is a sensor so we expect it to send us
            // data. Therefore we will use Gpio.DIRECTION_IN.
            bus.setDirection(Gpio.DIRECTION_IN);
            // GPIO is binary (true or false) so we are declaring what voltage signal equates to
            // true, a low or high voltage. For our sensor according to the datasheet a low voltage
            // means movement. Therefore we will use Gpio.ACTIVE_LOW.
            bus.setActiveType(Gpio.ACTIVE_LOW);
            // This is what state change we want monitor, from high voltage to low, low to high or
            // both. Meaning we could listen for movement starting, ending or both. We want to
            // listen for movement starting. Therefore we will use Gpio.EDGE_RISING.
            bus.setEdgeTriggerType(Gpio.EDGE_BOTH);
        } catch (IOException e) {
            throw new IllegalStateException("Sensor can't start - App is foobar'd", e);
        }
        try {
            bus.registerGpioCallback(callback);
        } catch (IOException e) {
            throw new IllegalStateException("Sensor can't register callback - App is foobar'd", e);
        }
    }

    private final GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            for (Listener listener : listeners) {
                listener.onMovement(gpio);
            }
            return true; // True to continue listening
        }
    };

    @Override
    public void shutdown() {
        bus.unregisterGpioCallback(callback);
        try {
            bus.close();
        } catch (IOException e) {
            Log.e("===lizhi", "Failed to shut down. You might get errors next time you try to start.", e);
        }
    }

}