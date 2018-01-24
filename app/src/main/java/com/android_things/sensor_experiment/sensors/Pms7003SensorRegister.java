package com.android_things.sensor_experiment.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.controllers.MainUiController;
import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorData;
import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorDriver;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/24/18.
 */

public class Pms7003SensorRegister extends SensorRegisterBase {
    Pms7003SensorRegister(Context context, SensorManager sensorManager,
                          MainUiController mainUiController) {
        super(context, sensorManager, mainUiController);
    }

    private Pms7003SensorDriver mDriver;
    private SensorEventListener mListener;

    @Override
    public boolean isSensorEnabled() {
        return Features.PMS_7003_SENSOR_ENABLED;
    }

    @Override
    public void start() {
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                int[] data = new int[3];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (int)event.values[i];
                }
                Pms7003SensorData structuredData = new Pms7003SensorData(data);
                mMainUiController.onPms7003SensorData(structuredData);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing for now.
            }
        };
        mSensorManager.registerDynamicSensorCallback(
                new SensorManager.DynamicSensorCallback() {
                    @Override
                    public void onDynamicSensorConnected(Sensor sensor) {
                        if (sensor.getType() ==  Sensor.TYPE_DEVICE_PRIVATE_BASE
                                && sensor.getStringType() == Pms7003SensorDriver.SENSOR_STRING_TYPE) {
                            mSensorManager.registerListener(
                                    mListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                });

        try {
            mDriver = new Pms7003SensorDriver();
        } catch (IOException e) {
            Log.e(TAG, "start: ", e);
        }
        mDriver.registerSensor();
    }

    @Override
    public void shutdown() {
        mSensorManager.unregisterListener(mListener);
        mDriver.unregisterSensor();
        mDriver.close();
    }
}
