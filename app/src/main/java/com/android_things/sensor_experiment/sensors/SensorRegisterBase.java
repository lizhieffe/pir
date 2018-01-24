package com.android_things.sensor_experiment.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

import com.android_things.sensor_experiment.controllers.MainUiController;

/**
 * Created by lizhieffe on 1/21/18.
 */

abstract public class SensorRegisterBase {
    Context mContext;
    SensorManager mSensorManager;
    MainUiController mMainUiController;

    SensorRegisterBase(Context context, SensorManager sensorManager,
                       MainUiController mainUiController) {
        mContext = context;
        mSensorManager = sensorManager;
        mMainUiController = mainUiController;
    }

    abstract public boolean isSensorEnabled();
    abstract public void start();
    abstract public void shutdown();
}
