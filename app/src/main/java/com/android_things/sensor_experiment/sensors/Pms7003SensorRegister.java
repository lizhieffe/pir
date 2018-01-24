package com.android_things.sensor_experiment.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003Sensor;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/24/18.
 */

public class Pms7003SensorRegister extends SensorRegisterBase {
    Pms7003SensorRegister(Activity activity,
                          Context context, SensorManager sensorManager) {
        super(activity, context, sensorManager);
    }

    private Pms7003Sensor mSensor;

    @Override
    public boolean isSensorEnabled() {
        return Features.PMS_7003_SENSOR_ENABLED;
    }

    private class ReadRunnable implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                Log.d(TAG, "Pms7003SensorRegister.run: reading data");
                mSensor.read();
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Pms7003SensorRegister.run: ", e);
            } finally {
                AsyncTask.execute(new ReadRunnable());
            }
        }

    }
    @Override
    public void start() {
        mSensor = new Pms7003Sensor();
        try {
            mSensor.startup();
        } catch (IOException e) {
            Log.e(TAG, "Pms7003SensorRegister.start: ", e);
        }

        // AsyncTask.execute(new ReadRunnable());
    }

    @Override
    public void shutdown() {
        mSensor.shutdown();
    }
}
