package com.android_things.sensor_experiment.sensors;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by lizhieffe on 12/30/17.
 */

public class HcSr04SensorDriver implements AutoCloseable {
    private static final String DRIVER_VENDOR = "DiyMall";
    private static final String DRIVER_NAME = "HC_SR501";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    HcSr04Sensor mDevice;
    HcSr04SensorUserDriver mUserDriver;

    public HcSr04SensorDriver() {
        mDevice = new HcSr04Sensor();
        mDevice.startup();
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }

        if (mUserDriver == null) {
            mUserDriver = new HcSr04SensorUserDriver();
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

    private class HcSr04SensorUserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_PROXIMITY)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMinDelay(HcSr04Sensor.SENSOR_READ_INTERVAL_MS * 1000)
                        .setMaxDelay(HcSr04Sensor.SENSOR_READ_INTERVAL_MS * 1000 * 2)
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{mDevice.readDistanceSync()});
        }
    }
}
