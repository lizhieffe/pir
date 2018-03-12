package com.zll.androidthings.drivers.hc_sr501_sensor;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by lizhieffe on 1/2/18.
 */

public class HcSr501SensorDriver implements AutoCloseable {
    private static final String DRIVER_VENDOR = "DiyMall";
    private static final String DRIVER_NAME = "HC_SR501";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private HcSr501Sensor mDevice;
    HcSr501SensorUserDriver mUserDriver;

    public HcSr501SensorDriver() {
        mDevice = new HcSr501Sensor();
        mDevice.startup();
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }

        if (mUserDriver == null) {
            mUserDriver = new HcSr501SensorUserDriver();
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

    private class HcSr501SensorUserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_MOTION_DETECT)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMinDelay(100 * 1000)  // 100ms
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
