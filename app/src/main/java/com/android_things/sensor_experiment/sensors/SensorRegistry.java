package com.android_things.sensor_experiment.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorDriver;
import com.android_things.sensor_experiment.indicator.AccelerometerUiController;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhi on 1/12/18.
 */

public class SensorRegistry {
    private SensorManager mSensorManager;

    private Mpu6500SensorDriver mMpu6500SensorDriver;
    private SensorEventListener mMpu6500SensorListener;
    private AccelerometerUiController mAccelUiController;

    public SensorRegistry(SensorManager sensorManager, TextView accelView) {
        mSensorManager = sensorManager;
        mAccelUiController = new AccelerometerUiController(accelView);
    }

    public void start() {
        maybeStartMpu6500Sensor();
    }

    public void shutdown() {
        mSensorManager.unregisterListener(mMpu6500SensorListener);
        mMpu6500SensorDriver.unregisterSensor();
    }

    private void maybeStartMpu6500Sensor() {
        mMpu6500SensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "onSensorChanged: " + event.values[0]
                        + " " + event.values[1] + " " + event.values[2]);
                float[] accelData = new float[3];
                for (int i = 0; i < accelData.length; i++) {
                    accelData[i] = event.values[i];
                }
                mAccelUiController.onAccelData(accelData);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing for now.
            }
        };
        mSensorManager.registerDynamicSensorCallback(
                new SensorManager.DynamicSensorCallback() {
                    @Override
                    public void onDynamicSensorConnected(Sensor sensor) {
                        if (sensor.getType() == sensor.TYPE_ACCELEROMETER) {
                            mSensorManager.registerListener(mMpu6500SensorListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                }
        );
        mMpu6500SensorDriver = new Mpu6500SensorDriver();
        mMpu6500SensorDriver.registerSensor();
    }
}
