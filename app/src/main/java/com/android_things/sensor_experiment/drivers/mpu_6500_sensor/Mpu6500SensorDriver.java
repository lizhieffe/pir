package com.android_things.sensor_experiment.drivers.mpu_6500_sensor;

import android.hardware.Sensor;
import android.util.Log;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhi on 1/12/18.
 */

public class Mpu6500SensorDriver implements AutoCloseable {
    private static final String DRIVER_VENDOR = "InvenSense";
    private static final String DRIVER_NAME = "MPU_6500";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private Mpu6500Sensor mDevice;
    Mpu6500SensorUserDriver mUserDriver;

    public Mpu6500SensorDriver() {
        mDevice = new Mpu6500Sensor();
        try {
            mDevice.startup();
        } catch (IOException e) {
            Log.e(TAG, "Mpu6500SensorDriver: ", e);
        }
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }

        if (mUserDriver == null) {
            mUserDriver = new Mpu6500SensorUserDriver();
            UserDriverManager.getManager()
                    .registerSensor(mUserDriver.getUserSensor());
        }
    }

    public void unregisterSensor() {
        if (mUserDriver != null) {
            UserDriverManager.getManager()
                    .unregisterSensor(mUserDriver.getUserSensor());
            mUserDriver = null;
        }
    }

    @Override
    public void close() {
        unregisterSensor();
        mDevice.shutdown();
        mDevice = null;
    }

    private class Mpu6500SensorUserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_ACCELEROMETER)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        // TODO: change the value dynamically based on sample rate
                        .setMinDelay(1 * 1000)  // 1ms. The default sample rate for MPU6500 accel and gyro sensors are 1kHz.
                        .setMaxDelay(10 * 1000)  // 10 ms
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            float accelData[] = mDevice.readAccelData();
            return new UserSensorReading(accelData);
        }
    }
}