package com.android_things.sensor_experiment.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.android_things.sensor_experiment.base.Constants;
import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorAccelDriver;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorDriverFactory;
import com.android_things.sensor_experiment.drivers.mpu_6500_sensor.Mpu6500SensorGyroDriver;
import com.android_things.sensor_experiment.indicator.AccelerometerUiController;
import com.android_things.sensor_experiment.indicator.Bme280UiController;
import com.android_things.sensor_experiment.logger.Mpu6500SensorLogger;
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;

import java.io.IOException;

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
    private TextView mAccelView;
    private TextView mGyroView;

    private SensorEventListener mBme280SensorTempuratureListener;
    private SensorEventListener mBme280SensorPressureListener;
    private SensorEventListener mBme280SensorHumidityListener;
    private Bmx280SensorDriver mBme280SensorDriver;
    private Bme280UiController mBme280UiController;
    private TextView mTemperatureView;
    private TextView mPressureView;
    private TextView mHumidityView;


    public SensorRegistry(Context context, SensorManager sensorManager,
                          TextView accelView, TextView gyroView,
                          TextView temperatureView, TextView pressureView,
                          TextView humidityView) {
        mContext = context;
        mSensorManager = sensorManager;

        mAccelView = accelView;
        mGyroView = gyroView;
        mTemperatureView = temperatureView;
        mPressureView = pressureView;
        mHumidityView = humidityView;
    }

    public void start() {
        maybeStartMpu6500Sensor();
        maybeStartBme280Sensor();
    }

    public void shutdown() {
        if (Features.ACCELEROMETER_ENABLED) {
            mSensorManager.unregisterListener(mMpu6500SensorAccelListener);
            mMpu6500SensorAccelDriver.unregisterSensor();
            mSensorManager.unregisterListener(mMpu6500SensorGyroListener);
            mMpu6500SensorGyroDriver.unregisterSensor();
        }

        if (Features.BME_280_SENSOR_ENABLED) {
            mSensorManager.unregisterListener(mBme280SensorTempuratureListener);
            mSensorManager.unregisterListener(mBme280SensorPressureListener);
            mSensorManager.unregisterListener(mBme280SensorHumidityListener);
            mBme280SensorDriver.unregisterTemperatureSensor();
            mBme280SensorDriver.unregisterPressureSensor();
            mBme280SensorDriver.unregisterHumiditySensor();

            try {
                mBme280SensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "SensorRegistry.shutdown: ", e);
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

    private void maybeStartBme280Sensor() {
        if (Features.BME_280_SENSOR_ENABLED) {
            mBme280UiController = new Bme280UiController(
                    mTemperatureView, mPressureView, mHumidityView);
            mBme280SensorTempuratureListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    // Result contains on number representing the current
                    // temperature in degrees Celsius.
                    Log.d(TAG, "SensorRegistry.onSensorChanged: on bme280 sensor temp data. length = "
                            + event.values.length);
                    Log.d(TAG, "SensorRegistry.onSensorChanged: bme280 temp data = " + event.values[0]);
                    mBme280UiController.onTemperatureData(event.values[0]);
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
                    Log.d(TAG, "SensorRegistry.onSensorChanged: on bme280 sensor pressure data. length = "
                            + event.values.length);
                    Log.d(TAG, "SensorRegistry.onSensorChanged: bme280 pressure data = " + event.values[0]);
                    mBme280UiController.onPressureData(event.values[0]);
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
                    Log.d(TAG, "SensorRegistry.onSensorChanged: on bme280 sensor humidity data. length = "
                            + event.values.length);
                    Log.d(TAG, "SensorRegistry.onSensorChanged: bme280 humidity data = " + event.values[0]);
                    mBme280UiController.onHumidityData(event.values[0]);
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
                                    mBme280SensorTempuratureListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        } else if (sensor.getType() == Sensor.TYPE_PRESSURE) {
                            Log.e(TAG, "SensorRegistry.onDynamicSensorConnected: ==== pressure sensor");
                            mSensorManager.registerListener(
                                    mBme280SensorPressureListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        } else if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                            Log.e(TAG, "SensorRegistry.onDynamicSensorConnected: ==== humidity sensor");
                            mSensorManager.registerListener(
                                    mBme280SensorHumidityListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                });

            try {
                mBme280SensorDriver = new Bmx280SensorDriver(
                        Constants.RPI_3_I2C_BUS);
                mBme280SensorDriver.registerTemperatureSensor();
                mBme280SensorDriver.registerPressureSensor();
                mBme280SensorDriver.registerHumiditySensor();
            } catch (IOException e) {
                Log.e(TAG, "SensorRegistry.maybeStartBme280Sensor: ", e);
            }
        }
    }
}
