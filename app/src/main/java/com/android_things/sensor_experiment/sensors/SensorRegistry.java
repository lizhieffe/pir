package com.android_things.sensor_experiment.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.controllers.MainUiController;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorAccelDriver;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorDriverFactory;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorGyroDriver;
import com.android_things.sensor_experiment.controllers.AccelerometerUiController;
import com.android_things.sensor_experiment.logger.Mpu6500SensorLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for sensors which manages the sensor resource and listeners.
 */

public class SensorRegistry {
    private Activity mActivity;
    private Context mContext;
    private SensorManager mSensorManager;

    private Mpu6500SensorAccelDriver mMpu6500SensorAccelDriver;
    private SensorEventListener mMpu6500SensorAccelListener;
    private Mpu6500SensorGyroDriver mMpu6500SensorGyroDriver;
    private SensorEventListener mMpu6500SensorGyroListener;
    private AccelerometerUiController mAccelUiController;
    private Mpu6500SensorLogger mMpu6500SensorLogger;
    private Mpu6500SensorDriverFactory mMpu6500SensorDriverFactory;
    private TextView mAccelView;
    private TextView mGyroView;

    private List<SensorRegisterBase> mSensorRegisters;


    private MainUiController mMainUiController;

    public SensorRegistry(Activity activity, Context context,
                          SensorManager sensorManager,
                          TextView accelView, TextView gyroView) {
        mActivity = activity;
        mContext = context;
        mSensorManager = sensorManager;

        mAccelView = accelView;
        mGyroView = gyroView;

        mMainUiController = new MainUiController(mActivity);
    }

    public void start() {
        maybeStartMpu6500Sensor();

        mSensorRegisters = new ArrayList<>();
        mSensorRegisters.add(new Bme280SensorRegister(
                mContext, mSensorManager, mMainUiController));
        mSensorRegisters.add(new HcSr04SensorRegister(
                mContext, mSensorManager, mMainUiController, mActivity));
        mSensorRegisters.add(new HcSr501SensorRegister(
                mContext, mSensorManager, mMainUiController, mActivity));
        mSensorRegisters.add(new Pms7003SensorRegister(
                mContext, mSensorManager, mMainUiController));
        mSensorRegisters.add(new Tcs34725SensorRegister(
                mContext, mSensorManager, mMainUiController));

        for (SensorRegisterBase srb : mSensorRegisters) {
            if (srb.isSensorEnabled()) {
                srb.start();
            }
        }
    }

    public void shutdown() {
        if (Features.ACCELEROMETER_ENABLED) {
            mSensorManager.unregisterListener(mMpu6500SensorAccelListener);
            mMpu6500SensorAccelDriver.unregisterSensor();
            mSensorManager.unregisterListener(mMpu6500SensorGyroListener);
            mMpu6500SensorGyroDriver.unregisterSensor();
        }

        for (SensorRegisterBase srb : mSensorRegisters) {
            if (srb.isSensorEnabled()) {
                srb.shutdown();
            }
        }
    }

    private void maybeStartMpu6500Sensor() {
        if (Features.ACCELEROMETER_ENABLED) {
            mMpu6500SensorDriverFactory = new Mpu6500SensorDriverFactory();
            mAccelUiController = new AccelerometerUiController(
                    mAccelView, mGyroView);
            mMpu6500SensorLogger = new Mpu6500SensorLogger(mContext);

            mMpu6500SensorAccelListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
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
}
