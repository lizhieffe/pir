package com.android_things.sensor_experiment.drivers.pms_7003;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by lizhi on 1/24/18.
 */

public class Pms7003SensorDriver implements AutoCloseable {
    public static final String SENSOR_STRING_TYPE = "air_quality";

    private static final String DRIVER_VENDOR = "Plantower";
    private static final String DRIVER_NAME = "PMS-5003";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private Pms7003Sensor mDevice;
    Pms7003SensorUserDriver mUserDriver;

    public Pms7003SensorDriver() throws IOException {
        mDevice = new Pms7003Sensor();
        mDevice.startup();
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }

        if (mUserDriver == null) {
            mUserDriver = new Pms7003SensorUserDriver();
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

    private class Pms7003SensorUserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setCustomType(Sensor.TYPE_DEVICE_PRIVATE_BASE,
                                SENSOR_STRING_TYPE, Sensor.REPORTING_MODE_CONTINUOUS)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        // TODO: find the best values from datasheet.
                        .setMinDelay(2000 * 1000)  // 2000 ms
                        .setMaxDelay(2500 * 1000)  // 2500 ms
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            float[] data = mDevice.read().toFloatArray();
            return new UserSensorReading(data);
        }
    }
}
