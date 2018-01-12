package com.android_things.sensor_experiment.drivers.mpu_6500;

/**
 * Created by lizhi on 1/12/18.
 */

public interface Mpu6500SensorListener {
    public void onAccelData(float[] data);
}
