package com.android_things.sensor_experiment.drivers.mpu_6500_sensor;

import android.util.Log;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhi on 1/12/18.
 */

public class Mpu6500SensorDriverFactory {
    private Mpu6500Sensor mSensor;

    private int mDriverCount = 0;

    public Mpu6500SensorAccelDriver createAccelDriver() {
        ++mDriverCount;
        return new Mpu6500SensorAccelDriver(getOrCreateSensor(), this);
    }

    public Mpu6500SensorGyroDriver createGyroDriver() {
        ++mDriverCount;
        return new Mpu6500SensorGyroDriver(getOrCreateSensor(), this);
    }

    public void shutdownDriver() {
        if (--mDriverCount == 0) {
            mSensor.shutdown();
            mSensor = null;
        }
    }

    private Mpu6500Sensor getOrCreateSensor() {
        if (mSensor == null) {
            mSensor = new Mpu6500Sensor();
            try {
                mSensor.startup();
            } catch (IOException e) {
                Log.e(TAG, "getOrCreateSensor: ", e);
            }
        }
        return mSensor;
    }
}
