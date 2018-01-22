package com.android_things.sensor_experiment.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

/**
 * Created by lizhieffe on 1/21/18.
 */

abstract public class SensorRegisterBase {
    // TODO: passing activity looks not good. Instead makes the activity UI controller
    // a listener to this, etc.
    Activity mActivity;
    Context mContext;
    SensorManager mSensorManager;

    SensorRegisterBase(Activity activity,
                       Context context, SensorManager sensorManager) {
        mActivity = activity;
        mContext = context;
        mSensorManager = sensorManager;
    }

    abstract public boolean isSensorEnabled();
    abstract public void start();
    abstract public void shutdown();
}
