package com.android_things.sensor_experiment.drivers.ccs_811_sensor;

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

public class Ccs811SensorDriver implements AutoCloseable {
    public static final String SENSOR_STRING_TYPE = "air_quality_sensor";

    private static final String DRIVER_VENDOR = "Sparkfun";
    private static final String DRIVER_NAME = "Ccs811";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private Ccs811Sensor mDevice;
    private Ccs811UserDriver mUserDriver;

    public Ccs811SensorDriver() throws IOException {
        mDevice = new Ccs811Sensor();
        mDevice.setMode(Ccs811Sensor.MODE_10S);
    }

    public Ccs811SensorDriver(String bus, int address) throws IOException {
        mDevice = new Ccs811Sensor(bus, address);
        mDevice.setMode(Ccs811Sensor.MODE_10S);
    }

    @Override
    public void close() throws IOException {
        try {
            mDevice.close();
        } finally {
            mDevice = null;
        }
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }
        if (mUserDriver == null) {
            mUserDriver = new Ccs811UserDriver();
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

    private class Ccs811UserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setCustomType(Sensor.TYPE_DEVICE_PRIVATE_BASE,
                                SENSOR_STRING_TYPE, Sensor.REPORTING_MODE_CONTINUOUS)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setMinDelay(20000000)  // 20 seconds
                        .setMaxDelay(30000000)
                        .setDriver(this)
                        .setUuid(UUID.randomUUID())
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{
                    mDevice.readAlgorithmResults()[0],
                    mDevice.readAlgorithmResults()[1],
                    mDevice.readAlgorithmResults()[2],
                    mDevice.readAlgorithmResults()[3],
            });
        }
    }
}
