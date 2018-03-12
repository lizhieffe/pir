package com.zll.androidthings.drivers.tcs_34725;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by lizhieffe on 1/21/18.
 */

public class Tcs34725SensorDriver implements AutoCloseable {
    private static final String TAG = "TCS-34725 Sensor Driver";

    public static final String SENSOR_STRING_TYPE = "RGB_sensor";

    private static final String DRIVER_VENDOR = "Adafruit";
    private static final String DRIVER_NAME = "TCS-34725";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private Tcs34725Sensor mDevice;
    private Tcs34725SensorUserDriver mUserDriver;

    public Tcs34725SensorDriver() throws IOException {
        mDevice = new Tcs34725Sensor();
        mDevice.startup();
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }

        if (mUserDriver == null) {
            mUserDriver = new Tcs34725SensorUserDriver();
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

    private class Tcs34725SensorUserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setCustomType(Sensor.TYPE_DEVICE_PRIVATE_BASE,
                                SENSOR_STRING_TYPE, Sensor.REPORTING_MODE_CONTINUOUS)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMinDelay(1000 * 1000)  // 100ms
                        .setMaxDelay(1500 * 1000)  // 150 ms
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            int[] color = mDevice.readColor().toIntArray();
            return new UserSensorReading(new float[]{
                    color[0],
                    color[1],
                    color[2],
                    color[3]});
        }
    }
}
