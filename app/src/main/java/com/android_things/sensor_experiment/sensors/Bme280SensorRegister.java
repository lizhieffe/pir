package com.android_things.sensor_experiment.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.android_things.sensor_experiment.base.Constants;
import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.controllers.MainUiController;
import com.android_things.sensor_experiment.logger.Bme280SensorLogger;
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/24/18.
 */

public class Bme280SensorRegister extends SensorRegisterBase {
    private Bmx280SensorDriver mDriver;

    private SensorEventListener mBme280SensorTemperatureListener;
    private SensorEventListener mBme280SensorPressureListener;
    private SensorEventListener mBme280SensorHumidityListener;
    private Bme280SensorLogger mBme280SensorLogger;

    Bme280SensorRegister(Context context, SensorManager sensorManager,
                         MainUiController mainUiController) {
        super(context, sensorManager, mainUiController);
    }

    @Override
    public boolean isSensorEnabled() {
        return Features.BME_280_SENSOR_ENABLED;
    }

    @Override
    public void start() {
        mBme280SensorLogger = new Bme280SensorLogger(mContext);

        mBme280SensorTemperatureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Result contains on number representing the current
                // temperature in degrees Celsius.
                mMainUiController.onTemperatureData(event.values[0]);
                mBme280SensorLogger.onTemperatureData(event.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing for now.
            }
        };
        mBme280SensorPressureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Result contains on number representing the current
                // barometric pressure in hPa units.
                mMainUiController.onPressureData(event.values[0]);
                mBme280SensorLogger.onPressureData(event.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing for now.
            }
        };
        mBme280SensorHumidityListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Result contains on number representing the current
                // relative humidity in RH percentage (100f means totally saturated air).
                mMainUiController.onHumidityData(event.values[0]);
                mBme280SensorLogger.onHumidityData(event.values[0]);
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
                        if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                            mSensorManager.registerListener(
                                    mBme280SensorTemperatureListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        } else if (sensor.getType() == Sensor.TYPE_PRESSURE) {
                            mSensorManager.registerListener(
                                    mBme280SensorPressureListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        } else if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                            mSensorManager.registerListener(
                                    mBme280SensorHumidityListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                });

        try {
            mDriver = new Bmx280SensorDriver(Constants.RPI_3_I2C_BUS);
            mDriver.registerTemperatureSensor();
            mDriver.registerPressureSensor();
            mDriver.registerHumiditySensor();
        } catch (IOException e) {
            Log.e(TAG, "SensorRegistry.maybeStartBme280Sensor: ", e);
        }
    }

    @Override
    public void shutdown() {
        mSensorManager.unregisterListener(mBme280SensorTemperatureListener);
        mSensorManager.unregisterListener(mBme280SensorPressureListener);
        mSensorManager.unregisterListener(mBme280SensorHumidityListener);
        mDriver.unregisterTemperatureSensor();
        mDriver.unregisterPressureSensor();
        mDriver.unregisterHumiditySensor();

        try {
            mDriver.close();
        } catch (IOException e) {
            Log.e(TAG, "SensorRegistry.shutdown: ", e);
        }
    }
}
