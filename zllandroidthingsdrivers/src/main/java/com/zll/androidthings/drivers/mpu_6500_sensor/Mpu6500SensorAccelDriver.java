package com.zll.androidthings.drivers.mpu_6500_sensor;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by lizhi on 1/12/18.
 */

public class Mpu6500SensorAccelDriver implements AutoCloseable {
    private static final String TAG = "MPU 6500 Sensor Accel Driver";
    private static final String DRIVER_VENDOR = "InvenSense";
    private static final String DRIVER_NAME = "MPU-6500 Accel";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private Mpu6500Sensor mDevice;
    Mpu6500SensorUserDriver mUserDriver;
    Mpu6500SensorDriverFactory mFactory;

    Mpu6500SensorAccelDriver(Mpu6500Sensor sensor, Mpu6500SensorDriverFactory factory) {
        mDevice = sensor;
        mFactory = factory;
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
        mFactory.shutdownDriver();
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
                        .setMinDelay(30 * 1000)  // 30 ms. Note that the default sample rate for MPU6500 accel and gyro sensors are 1kHz.
                        .setMaxDelay(150 * 1000)  // 150 ms
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
