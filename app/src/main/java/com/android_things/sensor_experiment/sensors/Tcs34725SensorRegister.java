package com.android_things.sensor_experiment.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.controllers.MainUiController;
import com.android_things.sensor_experiment.controllers.RgbUiController;
import com.android_things.sensor_experiment.drivers.tcs_34725.Color;
import com.android_things.sensor_experiment.drivers.tcs_34725.Tcs34725SensorDriver;
import com.android_things.sensor_experiment.pir.sensor_test.R;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/21/18.
 */

public class Tcs34725SensorRegister extends SensorRegisterBase {
    Tcs34725SensorRegister(Activity activity, Context context, SensorManager sensorManager,
                           MainUiController mainUiController) {
        super(activity, context, sensorManager, mainUiController);
    }

    private Tcs34725SensorDriver mDriver;
    private SensorEventListener mListener;

    private RgbUiController mUiController;

    @Override
    public boolean isSensorEnabled() {
        return Features.TCS_34725_SENSOR_ENABLED;
    }

    @Override
    public void start() {
        mUiController = new RgbUiController(
                (TextView) mActivity.findViewById(R.id.rgb_text_view));

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

                mUiController.onColorData(new Color(
                        (int) event.values[0],
                        (int) event.values[1],
                        (int) event.values[2],
                        (int) event.values[3]
                ));
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
