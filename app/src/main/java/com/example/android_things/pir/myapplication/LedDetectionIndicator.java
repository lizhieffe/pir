package com.example.android_things.pir.myapplication;

import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by lizhieffe on 12/23/17.
 */

class LedDetectionIndicator
        implements MotionSensor.Listener, DetectionIndicator {
    private Gpio ledBus = null;

    private long last_indication_unix_time_ms = 0;
    final private int indication_cool_down_ms = 500;
    final private int indication_cool_down_error_ms = 50;

    @Override
    public void start() {
        initLedGpioBus();
    }

    @Override
    public void close() {
        if (ledBus != null) {
            try {
                ledBus.close();
            } catch (IOException e) {
                Log.e("===lizhi", "Error on PeripheralIO API", e);
            }
        }
    }

    @Override
    public void onMovement(Gpio gpio) {
        try {
            if (ledBus != null) {
                ledBus.setValue(gpio.getValue());
                if (gpio.getValue() == true) {
                    if (System.currentTimeMillis() - last_indication_unix_time_ms
                            >= indication_cool_down_ms) {
                        ledBus.setValue(true);
                        last_indication_unix_time_ms = System.currentTimeMillis();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    ledBus.setValue(false);
                                } catch (IOException e) {
                                    throw new IllegalStateException("Cannot set led: ", e);
                                }
                            }
                        }, indication_cool_down_ms - indication_cool_down_error_ms);
                    }
                }
            }

            Log.d("lizhi===", "MOVEMENT DETECTED with GPIO value: " + gpio.getValue());
        } catch (IOException e) {
            throw new IllegalStateException("Can't get GPIO value: ", e);
        }

    }

    private void initLedGpioBus() {
        try {
            ledBus = new PeripheralManagerService().openGpio("BCM26");
            ledBus.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            throw new IllegalStateException("Can't open GPIO - can't create app.", e);
        }
    }
}
