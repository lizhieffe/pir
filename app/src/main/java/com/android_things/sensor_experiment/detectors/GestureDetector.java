package com.android_things.sensor_experiment.detectors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.android_things.sensor_experiment.sensors.zx_gesture.ZxGestureSensor;
import com.android_things.sensor_experiment.sensors.zx_gesture.ZxGestureSensorDriver;
import com.android_things.sensor_experiment.utils.EnvDetector;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/2/18.
 */

public class GestureDetector implements EnvDetector {
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    private ZxGestureSensorDriver mZxGestureSensorDriver;

    public GestureDetector(SensorManager sensorManager) {
        mSensorManager = sensorManager;
    }

    @Override
    public void start() {
        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "GestureDetector.onSensorChanged: value = "
                        + ZxGestureSensor.Gesture.getGesture((int)event.values[0]));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d(TAG, "GestureDetector.onAccuracyChanged: ");
            }
        };
        mSensorManager.registerDynamicSensorCallback(
                new SensorManager.DynamicSensorCallback() {
                    @Override
                    public void onDynamicSensorConnected(Sensor sensor) {
                        if (sensor.getType() ==  Sensor.TYPE_DEVICE_PRIVATE_BASE
                                    && sensor.getStringType() == ZxGestureSensorDriver.SENSOR_STRING_TYPE) {
                            mSensorManager.registerListener(mSensorListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                });

        mZxGestureSensorDriver = new ZxGestureSensorDriver();
        mZxGestureSensorDriver.registerSensor();
    }

    @Override
    public void shutdown() {
        mSensorManager.unregisterListener(mSensorListener);
        mZxGestureSensorDriver.unregisterSensor();
    }
}
