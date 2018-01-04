package com.android_things.sensor_experiment.detectors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.util.Log;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.drivers.hc_sr04_sensor.HcSr04SensorDriver;
import com.android_things.sensor_experiment.drivers.hc_sr501_sensor.HcSr501Sensor;
import com.android_things.sensor_experiment.drivers.hc_sr501_sensor.HcSr501SensorDriver;
import com.android_things.sensor_experiment.utils.EnvDetector;

import java.util.ArrayList;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 12/24/17.
 */

public class MotionDetector implements EnvDetector {
    private SensorManager mSensorManager;

    private Sensor mPirSensor;
    private HcSr501SensorDriver mPirSensorDriver;
    private TriggerEventListener mPirSensorListener;
    private long mPirDelayStarts;

    private HcSr04SensorDriver mProximitySensorDriver;
    private SensorEventListener mProximitySensorListener;

    private List<MotionDetectorListener> mListener;

    private float mPrevDistance = 0;

    public MotionDetector(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mListener = new ArrayList<>();
    }

    @Override
    public void start() {
        if (Features.MOTION_DETECTION_PIR_ENABLED) {
            mPirSensorListener = new TriggerEventListener() {
                @Override
                public void onTrigger(TriggerEvent event) {
                    if (event.values[0] > 0
                            && System.currentTimeMillis() - mPirDelayStarts > HcSr501Sensor.DELAY_TIME_MS) {
                        mPirDelayStarts = System.currentTimeMillis();
                        MotionDetectionEvent motionDetectionEvent
                                = new MotionDetectionEvent();
                        motionDetectionEvent.mSource
                                = MotionDetectionEvent.Source.PIR;
                        notifyListeners(motionDetectionEvent);
                    }
                    mSensorManager.requestTriggerSensor(mPirSensorListener, mPirSensor);

                }
            };
            mSensorManager.registerDynamicSensorCallback(
                    new SensorManager.DynamicSensorCallback() {
                        @Override
                        public void onDynamicSensorConnected(Sensor sensor) {
                            if (sensor.getType() == sensor.TYPE_MOTION_DETECT) {
                                mSensorManager.requestTriggerSensor(mPirSensorListener, sensor);
                                mPirSensor = sensor;
                            }
                        }
                    });
            mPirSensorDriver = new HcSr501SensorDriver();
            mPirSensorDriver.registerSensor();
        }

        if (Features.MOTION_DETECTION_PROXIMITY_ENABLED) {
            mProximitySensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float currDistance;
                    float prevDistance;
                    synchronized (this) {
                        currDistance = event.values[0];
                        Log.d(TAG, "MotionDetector.onSensorChanged: " + currDistance);
                        if (currDistance < 0) {
                            return;
                        }

                        prevDistance = mPrevDistance;
                        mPrevDistance = currDistance;
                    }
                    if (Math.abs(currDistance - prevDistance) > 30) {
                        MotionDetectionEvent motionDetectionEvent = new MotionDetectionEvent();
                        motionDetectionEvent.mSource = MotionDetectionEvent.Source.PROXIMITY;
                        motionDetectionEvent.mProxmityParam = currDistance;
                        notifyListeners(motionDetectionEvent);
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
                                mSensorManager.registerListener(mProximitySensorListener, sensor,
                                        SensorManager.SENSOR_DELAY_NORMAL);
                            }
                        }
                    });

            mProximitySensorDriver = new HcSr04SensorDriver();
            mProximitySensorDriver.registerSensor();
        }
    }

    @Override
    public void shutdown() {
        if (mListener != null) {
            mListener.clear();
        }

        if (Features.MOTION_DETECTION_PIR_ENABLED) {
            mSensorManager.cancelTriggerSensor(mPirSensorListener, mPirSensor);
            mPirSensorDriver.unregisterSensor();
        }

        if (Features.MOTION_DETECTION_PROXIMITY_ENABLED) {
            mSensorManager.unregisterListener(mProximitySensorListener);
            mProximitySensorDriver.unregisterSensor();
        }
    }

    public void addListener(MotionDetectorListener listener) {
        mListener.add(listener);
    }

    synchronized void notifyListeners(MotionDetectionEvent event) {
        Log.d(TAG, "MotionDetector.notifyListeners: event = " + event.toString());
        for (MotionDetectorListener listener : mListener) {
            listener.onDetected(event);
        }
    }
}
