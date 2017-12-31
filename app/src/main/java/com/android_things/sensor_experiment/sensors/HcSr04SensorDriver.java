package com.android_things.sensor_experiment.sensors;

/**
 * Created by lizhieffe on 12/30/17.
 */

public class HcSr04SensorDriver implements AutoCloseable {
    HcSr04Sensor mDevice;

    @Override
    public void close() {
        mDevice.shutdown();
    }
}
