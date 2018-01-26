package com.android_things.sensor_experiment.drivers.ncs_36000_sensor;

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
 * Created by lizhieffe on 1/25/18.
 */

public class Ncs36000SensorDriver implements AutoCloseable {
    private static final String DRIVER_VENDOR = "Sparkfun";
    private static final String DRIVER_NAME = "ncs-36000";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private Ncs36000Sensor mDevice;
    private Ncs36000SensorUserDriver mUserDriver;

    public Ncs36000SensorDriver() {
        mDevice = new Ncs36000Sensor();
        mDevice.startup();
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }

        if (mUserDriver == null) {
            mUserDriver = new Ncs36000SensorUserDriver();
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

    private class Ncs36000SensorUserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_MOTION_DETECT)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMinDelay(100 * 1000)  // 100 ms
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
            boolean detected  = mDevice.readData();
            return new UserSensorReading(new float[]{detected ? 1 : 0});
        }
    }
}
