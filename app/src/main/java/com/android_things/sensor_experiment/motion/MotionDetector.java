package com.android_things.sensor_experiment.motion;

import android.util.Log;

import com.android_things.sensor_experiment.sensors.MotionSensor;
import com.android_things.sensor_experiment.sensors.PirMotionSensor;
import com.android_things.sensor_experiment.sensors.ProximitySr04Sensor;
import com.google.android.things.pio.Gpio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 12/24/17.
 */

public class MotionDetector {
    private List<MotionDetectionListener> mListener;

    private PirMotionSensor mPirSensor;
    private final MotionSensor.Listener mPirSensorCallback
            = new MotionSensor.Listener() {
        @Override
        public void onMovement(Gpio gpio) {
            try {
                if (gpio.getValue()) {
                    notifyListeners();
                }
            } catch (IOException e) {
                Log.e(TAG, "Cannot get GPIO value: ", e);
            }

        }
    };

    private ProximitySr04Sensor mProximitySensor;
    private ExecutorService mProximitySensorCheckerExecutor;

    public MotionDetector() {
        mListener = new ArrayList<>();
        mPirSensor = new PirMotionSensor();
        mProximitySensor = new ProximitySr04Sensor();
    }

    public void start() {
        mPirSensor.startup();
        mPirSensor.addListener(mPirSensorCallback);

        mProximitySensor.startup();
        mProximitySensorCheckerExecutor = Executors.newSingleThreadExecutor();
        mProximitySensorCheckerExecutor.submit(new Runnable() {
            @Override
            public void run() {
                boolean is_first_iteration = true;
                double prev_distance = 0;
                double curr_distance;
                while (true) {
                    try {
                        curr_distance = mProximitySensor.readDistanceSync();
                        if (!is_first_iteration &&
                                Math.abs(curr_distance - prev_distance) > 30) {
                            notifyListeners();
                            Log.e(TAG, "Proximity detected. Distance is: " + curr_distance);
                        }
                        prev_distance = curr_distance;
                        if (is_first_iteration) {
                            is_first_iteration = false;
                        }
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot measure distance: ", e);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Cannot measure distance: ", e);
                    }
                }
            }
        });
    }

    public void shutdown() {
        if (mListener != null) {
            mListener.clear();
        }
        if (mPirSensor != null) {
            mPirSensor.shutdown();
            mPirSensor = null;
        }
        if (mProximitySensor != null) {
            mProximitySensor.shutdown();
            mProximitySensor = null;
        }
        if (mProximitySensorCheckerExecutor != null) {
            mProximitySensorCheckerExecutor.shutdown();
        }
    }

    public void addListener(MotionDetectionListener listener) {
        mListener.add(listener);
    }

    synchronized void notifyListeners() {
        for (MotionDetectionListener listener : mListener) {
            listener.onDetected();
        }
    }
}
