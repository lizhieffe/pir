package com.android_things.sensor_experiment.indicator;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android_things.sensor_experiment.detectors.MotionDetectionEvent;
import com.android_things.sensor_experiment.detectors.MotionDetectorListener;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 12/23/17.
 */

public class LedDetectorIndicator
        implements MotionDetectorListener, DetectionIndicator {
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
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }

    @Override
    synchronized public void onDetected(MotionDetectionEvent event) {
        try {
            if (ledBus != null) {
                if (System.currentTimeMillis() - last_indication_unix_time_ms
                        >= indication_cool_down_ms) {
                    ledBus.setValue(true);
                    last_indication_unix_time_ms = System.currentTimeMillis();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
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
