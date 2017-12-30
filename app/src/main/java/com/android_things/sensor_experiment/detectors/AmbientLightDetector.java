package com.android_things.sensor_experiment.detectors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.android_things.sensor_experiment.sensors.AmbientLightSen14350SensorDriver;
import com.android_things.sensor_experiment.utils.EnvDetector;

import java.util.ArrayList;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 12/30/17.
 */

public class AmbientLightDetector implements EnvDetector {
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    private AmbientLightSen14350SensorDriver mAmbientLightSensorDriver;

    List<AmbientLightListener> mListeners = new ArrayList<>();

    public AmbientLightDetector(SensorManager sensorManager) {
        mSensorManager = sensorManager;
    }

    @Override
    public void start() {
        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                for (AmbientLightListener listener : mListeners) {
                    listener.onDetected(sensorEvent.values[0]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        Log.d(TAG, "MainActivity.onDynamicSensorConnected: 000000000");
        mSensorManager.registerDynamicSensorCallback(
                new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                Log.d(TAG, "MainActivity.onDynamicSensorConnected: aaaaaaaa");
                if (sensor.getType() ==  Sensor.TYPE_LIGHT) {
                    Log.d(TAG, "MainActivity.onDynamicSensorConnected: bbbbbbbbbbb");
                    mSensorManager.registerListener(mSensorListener, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        mAmbientLightSensorDriver = new AmbientLightSen14350SensorDriver();
        mAmbientLightSensorDriver.registerSensor();
    }

    @Override
    public void shutdown() {
        mSensorManager.unregisterListener(mSensorListener);
        mAmbientLightSensorDriver.unregisterSensor();
    }

    public void addListener(AmbientLightListener listener) {
        mListeners.add(listener);
    }
}
