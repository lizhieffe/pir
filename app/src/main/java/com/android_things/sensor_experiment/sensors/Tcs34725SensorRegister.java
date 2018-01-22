package com.android_things.sensor_experiment.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.drivers.tcs_34725.Tcs34725SensorDriver;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/21/18.
 */

public class Tcs34725SensorRegister extends SensorRegisterBase {
    Tcs34725SensorRegister(Context context, SensorManager sensorManager) {
        super(context, sensorManager);
    }

    private Tcs34725SensorDriver mDriver;
    private SensorEventListener mListener;

    @Override
    public boolean isSensorEnabled() {
        return Features.TCS_34725_SENSOR_ENABLED;
    }

    @Override
    public void start() {
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                StringBuilder sb = new StringBuilder();
                sb.append(event.values[0]);
                sb.append(" ");
                sb.append(event.values[1]);
                sb.append(" ");
                sb.append(event.values[2]);
                sb.append(" ");
                sb.append(event.values[3]);
                // Log.d(TAG, "Tcs34725SensorRegister.onSensorChanged: data is " + sb.toString());
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
                        && sensor.getStringType() == Tcs34725SensorDriver.SENSOR_STRING_TYPE) {
                    mSensorManager.registerListener(
                            mListener, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        try {
            mDriver = new Tcs34725SensorDriver();
        } catch (IOException e) {
            Log.e(TAG, "Tcs34725SensorRegister.start: ", e);
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
