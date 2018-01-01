package com.android_things.sensor_experiment.detectors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.android_things.sensor_experiment.sensors.Ccs811SensorDriver;
import com.android_things.sensor_experiment.sensors.HcSr04Sensor;
import com.android_things.sensor_experiment.sensors.HcSr04SensorDriver;
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
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

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
    private float mPrevDistance = 0;
    private float mCurrDistance = 0;
    private final HcSr04Sensor.Listener mProximitySensorCallback
            = new HcSr04Sensor.Listener() {
        @Override
        public void onEvent(HcSr04Sensor.Event event) {
            mCurrDistance = event.distance;
            if (mCurrDistance >= 0) {
                if (Math.abs(mCurrDistance - mPrevDistance) > 30) {
                    MotionDetectionEvent motionDetectionEvent = new MotionDetectionEvent();
                    motionDetectionEvent.mSource = MotionDetectionEvent.Source.PROXIMITY;
                    motionDetectionEvent.mProxmityParam = mCurrDistance;
                    notifyListeners(motionDetectionEvent);
                }
                mPrevDistance = mCurrDistance;
            }
        }
    };

    private HcSr04SensorDriver mProximitySensorDriver;

    public MotionDetector(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mListener = new ArrayList<>();
        mPirSensor = new Sen13285Sensor();
        mProximitySensor = new HcSr04Sensor();
    }

    @Override
    public void start() {
        mPirSensor.addListener(mPirSensorCallback);
        mPirSensor.startup();

        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mCurrDistance = event.values[0];
                if (mCurrDistance >= 0) {
                    if (Math.abs(mCurrDistance - mPrevDistance) > 30) {
                        MotionDetectionEvent motionDetectionEvent = new MotionDetectionEvent();
                        motionDetectionEvent.mSource = MotionDetectionEvent.Source.PROXIMITY;
                        motionDetectionEvent.mProxmityParam = mCurrDistance;
                        notifyListeners(motionDetectionEvent);
                    }
                    mPrevDistance = mCurrDistance;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

        };
        mSensorManager.registerDynamicSensorCallback(
                new SensorManager.DynamicSensorCallback() {
                    @Override
                    public void onDynamicSensorConnected(Sensor sensor) {
                        if (sensor.getType() == sensor.TYPE_PROXIMITY) {
                            mSensorManager.registerListener(mSensorListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                });

        mProximitySensorDriver = new HcSr04SensorDriver();
        mProximitySensorDriver.registerSensor();
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
        mSensorManager.unregisterListener(mSensorListener);
        mProximitySensorDriver.unregisterSensor();
    }

    public void addListener(MotionDetectorListener listener) {
        mListener.add(listener);
    }

    synchronized void notifyListeners(MotionDetectionEvent event) {
        Log.d(TAG, "MotionDetector.notifyListeners: notifying listeners with event" + event.toString());
        for (MotionDetectorListener listener : mListener) {
            listener.onDetected(event);
        }
    }
}
