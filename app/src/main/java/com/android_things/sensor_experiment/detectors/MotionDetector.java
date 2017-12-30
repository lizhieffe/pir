package com.android_things.sensor_experiment.detectors;

import android.util.Log;

import com.android_things.sensor_experiment.sensors.HcSr04Sensor;
import com.android_things.sensor_experiment.sensors.MotionSensor;
import com.android_things.sensor_experiment.sensors.Sen13285Sensor;
import com.android_things.sensor_experiment.utils.EnvDetector;
import com.google.android.things.pio.Gpio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 12/24/17.
 */

public class MotionDetector implements EnvDetector {
    private List<MotionDetectorListener> mListener;

    private Sen13285Sensor mPirSensor;
    private final MotionSensor.Listener mPirSensorCallback
            = new MotionSensor.Listener() {
        @Override
        public void onMovement(Gpio gpio) {
            try {
                if (gpio.getValue()) {
                    MotionDetectionEvent event = new MotionDetectionEvent();
                    event.mSource = MotionDetectionEvent.Source.PIR;
                    notifyListeners(event);
                }
            } catch (IOException e) {
                Log.e(TAG, "Cannot get GPIO value: ", e);
            }

        }
    };

    private HcSr04Sensor mProximitySensor;
    private double mPrevDistance = 0;
    private double mCurrDistance = 0;
    private final HcSr04Sensor.Listener mProximitySensorCallback
            = new HcSr04Sensor.Listener() {
        @Override
        public void onEvent(HcSr04Sensor.Event event) {
            mCurrDistance = event.distance;
            if (Math.abs(mCurrDistance - mPrevDistance) > 30) {
                MotionDetectionEvent motionDetectionEvent = new MotionDetectionEvent();
                motionDetectionEvent.mSource = MotionDetectionEvent.Source.PROXIMITY;
                motionDetectionEvent.mProxmityParam = mCurrDistance;
                notifyListeners(motionDetectionEvent);
            }
            mPrevDistance = mCurrDistance;
        }
    };

    public MotionDetector() {
        mListener = new ArrayList<>();
        mPirSensor = new Sen13285Sensor();
        mProximitySensor = new HcSr04Sensor();
    }

    @Override
    public void start() {
        mPirSensor.addListener(mPirSensorCallback);
        mPirSensor.startup();

        mProximitySensor.addListener(mProximitySensorCallback);
        mProximitySensor.startup();
    }

    @Override
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
    }

    public void addListener(MotionDetectorListener listener) {
        mListener.add(listener);
    }

    synchronized void notifyListeners(MotionDetectionEvent event) {
        Log.d(TAG, "MotionDetector.notifyListeners: motion detected: "
                + event.toString());
        for (MotionDetectorListener listener : mListener) {
            listener.onDetected(event);
        }
    }
}
