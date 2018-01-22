package com.android_things.sensor_experiment.sensors;

import android.content.Context;
import android.hardware.SensorManager;

/**
 * Created by lizhieffe on 1/21/18.
 */

abstract public class SensorRegisterBase {
    Context mContext;
    SensorManager mSensorManager;

    SensorRegisterBase(Context context, SensorManager sensorManager) {
        mContext = context;
        mSensorManager = sensorManager;
    }

    abstract public boolean isSensorEnabled();
    abstract public void start();
    abstract public void shutdown();
}
