package com.android_things.sensor_experiment.sensors;

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
 * Created by lizhieffe on 12/28/17.
 */

public class Apds9301SensorDriver implements AutoCloseable {
    // Driver parameters.
    private static final String DRIVER_VENDOR = "Sparkfun";
    private static final String DRIVER_NAME = "Sen14350";
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private Apds9301Sensor mDevice;
    private AmbientLightUserDriver mUserDriver;

    public Apds9301SensorDriver() {
        mDevice = new Apds9301Sensor();
        try {
            mDevice.startup();
        } catch (IOException e) {
            Log.e(TAG, "Apds9301SensorDriver.Apds9301SensorDriver: ", e);
        }
    }

    public Apds9301SensorDriver(String bus, int address) {
        mDevice = new Apds9301Sensor(bus, address);
        try {
            mDevice.startup();
        } catch (IOException e) {
            Log.e(TAG, "Apds9301SensorDriver.Apds9301SensorDriver: ", e);
        }
    }

    public Apds9301Sensor getDevice() {
        return mDevice;
    }

    @Override
    public void close() throws IOException {
        unregisterSensor();
        if (mDevice != null) {
            try {
                mDevice.shutdown();
            } finally {
                mDevice = null;
            }
        }
    }

    public void registerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("Cannot register closed driver");
        }

        Log.d(TAG, "Apds9301SensorDriver.registerSensor: zzzzzzzzzzz");
        if (mUserDriver == null) {
            mUserDriver = new AmbientLightUserDriver();
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

    private class AmbientLightUserDriver extends UserSensorDriver {
        // TODO: figure out the correct value
        private static final float DRIVER_POWER = 1;
        // TODO: figure out the correct value
        // The min and max delay for measurements is affected by the configured integration time.
        private static final int DRIVER_MIN_DELAY_US = 80 * 1000;
        private static final int DRIVER_MAX_DELAY_US = 640 * 1000;

        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_LIGHT)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(mDevice.getCurrentMaxRange())
                        .setResolution(mDevice.getCurrentResolution())
                        .setPower(DRIVER_POWER)
                        .setMinDelay(DRIVER_MIN_DELAY_US)
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setMaxDelay(DRIVER_MAX_DELAY_US)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{mDevice.readLuxLevel()});
        }
    }
}
