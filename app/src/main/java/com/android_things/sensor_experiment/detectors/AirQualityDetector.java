package com.android_things.sensor_experiment.detectors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.zll.androidthings.drivers.ccs_811_sensor.Ccs811SensorDriver;
import com.android_things.sensor_experiment.utils.EnvDetector;

import java.io.IOException;

/**
 * Created by lizhieffe on 12/30/17.
 */

public class AirQualityDetector implements EnvDetector {
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    private Ccs811SensorDriver mDriver;

    public AirQualityDetector(SensorManager sensorManager) {
        mSensorManager = sensorManager;
    }

    @Override
    public void start() throws IOException {
        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Log.d(TAG, "AirQualityDetector.onSensorChanged: on event " + event.values[0]);
                // Log.d(TAG, "AirQualityDetector.onSensorChanged: on event " + event.values[1]);
                // Log.d(TAG, "AirQualityDetector.onSensorChanged: on event " + event.values[2]);
                // Log.d(TAG, "AirQualityDetector.onSensorChanged: on event " + event.values[3]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager.registerDynamicSensorCallback(
                new SensorManager.DynamicSensorCallback() {
                    @Override
                    public void onDynamicSensorConnected(Sensor sensor) {
                        if (sensor.getType() ==  Sensor.TYPE_DEVICE_PRIVATE_BASE
                                && sensor.getStringType() == Ccs811SensorDriver.SENSOR_STRING_TYPE) {
                            mSensorManager.registerListener(mSensorListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                });

        mDriver = new Ccs811SensorDriver();
        mDriver.registerSensor();
    }

    public void shutdown() {
        mSensorManager.unregisterListener(mSensorListener);
        mDriver.unregisterSensor();
    }
}
