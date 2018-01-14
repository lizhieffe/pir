package com.android_things.sensor_experiment.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorAccelDriver;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorDriverFactory;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorGyroDriver;
import com.android_things.sensor_experiment.indicator.AccelerometerUiController;
import com.android_things.sensor_experiment.logger.Mpu6500SensorLogger;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Registry for sensors which manages the sensor resource and listeners.
 */

public class SensorRegistry {
    private Context mContext;
    private SensorManager mSensorManager;

    private Mpu6500SensorAccelDriver mMpu6500SensorAccelDriver;
    private SensorEventListener mMpu6500SensorAccelListener;
    private Mpu6500SensorGyroDriver mMpu6500SensorGyroDriver;
    private SensorEventListener mMpu6500SensorGyroListener;

    private AccelerometerUiController mAccelUiController;
    private Mpu6500SensorLogger mMpu6500SensorLogger;

    private Mpu6500SensorDriverFactory mMpu6500SensorDriverFactory;

    public SensorRegistry(Context context, SensorManager sensorManager,
                          TextView accelView, TextView gyroView) {
        mContext = context;
        mSensorManager = sensorManager;

        mMpu6500SensorDriverFactory = new Mpu6500SensorDriverFactory();

        mAccelUiController = new AccelerometerUiController(accelView, gyroView);
        mMpu6500SensorLogger = new Mpu6500SensorLogger(mContext);
    }

    public void start() {
        maybeStartMpu6500Sensor();
    }

    public void shutdown() {
        mSensorManager.unregisterListener(mMpu6500SensorAccelListener);
        mMpu6500SensorAccelDriver.unregisterSensor();
        mSensorManager.unregisterListener(mMpu6500SensorGyroListener);
        mMpu6500SensorGyroDriver.unregisterSensor();
    }

    private void maybeStartMpu6500Sensor() {
        mMpu6500SensorAccelListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "onSensorChanged: accel " + event.values[0]
                        + " " + event.values[1] + " " + event.values[2]);
                float[] data = new float[3];
                for (int i = 0; i < data.length; i++) {
                    data[i] = event.values[i];
                }
                mAccelUiController.onAccelData(data);
                mMpu6500SensorLogger.onAccelData(data);
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
                            mSensorManager.registerListener(mMpu6500SensorAccelListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                }
        );
        mMpu6500SensorAccelDriver = mMpu6500SensorDriverFactory.createAccelDriver();
        mMpu6500SensorAccelDriver.registerSensor();

        mMpu6500SensorGyroListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "onSensorChanged: gyro " + event.values[0]
                        + " " + event.values[1] + " " + event.values[2]);
                float[] data = new float[3];
                for (int i = 0; i < data.length; i++) {
                    data[i] = event.values[i];
                }
                mAccelUiController.onGyroData(data);
                mMpu6500SensorLogger.onGyroData(data);
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
                        if (sensor.getType() == sensor.TYPE_GYROSCOPE) {
                            mSensorManager.registerListener(mMpu6500SensorGyroListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                }
        );
        mMpu6500SensorGyroDriver = mMpu6500SensorDriverFactory.createGyroDriver();
        mMpu6500SensorGyroDriver.registerSensor();
    }
}
