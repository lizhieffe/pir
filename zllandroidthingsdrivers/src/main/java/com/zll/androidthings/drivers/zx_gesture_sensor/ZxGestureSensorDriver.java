package com.zll.androidthings.drivers.zx_gesture_sensor;

import android.hardware.Sensor;
import android.util.Log;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by lizhieffe on 1/2/18.
 */

public class ZxGestureSensorDriver implements AutoCloseable {
    private static final String TAG = "ZX Gesture Sensor Driver";

    public static final String SENSOR_STRING_TYPE = "gesture_sensor";

    private static final String DRIVER_VENDOR = "Sparkfun";
    private static final String DRIVER_NAME = "ZX Gesture Sensor";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private ZxGestureSensorUart mDevice;
    private ZxGestureSensorUserDriver mUserDriver;

    public ZxGestureSensorDriver() {
        mDevice = new ZxGestureSensorUart();
    }

    public void close() {
        unregisterSensor();
        mDevice.shutdown();
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }
        if (mUserDriver == null) {
            try {
                mDevice.startup();
            } catch (IOException e) {
                Log.e(TAG, "ZxGestureSensorDriver.registerSensor: ", e);
            }
            mUserDriver = new ZxGestureSensorUserDriver();
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

    private class ZxGestureSensorUserDriver extends UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setCustomType(Sensor.TYPE_DEVICE_PRIVATE_BASE,
                                SENSOR_STRING_TYPE, Sensor.REPORTING_MODE_CONTINUOUS)
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
            return new UserSensorReading(new float[]{
                    ZxGestureSensor.Gesture.getGestureId(mDevice.readGesture())});
        }

    }
}
