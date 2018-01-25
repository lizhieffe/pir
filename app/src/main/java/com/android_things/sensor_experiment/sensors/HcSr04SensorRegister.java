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
import com.android_things.sensor_experiment.controllers.DistanceController;
import com.android_things.sensor_experiment.controllers.MainUiController;
import com.android_things.sensor_experiment.detectors.MotionDetectionEvent;
import com.android_things.sensor_experiment.drivers.hc_sr04_sensor.HcSr04SensorDriver;
import com.android_things.sensor_experiment.pir.sensor_test.R;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/24/18.
 */

public class HcSr04SensorRegister extends SensorRegisterBase {
    // TODO: Refactor the get UI part out of this class.
    private Activity mActivity;

    private HcSr04SensorDriver mDriver;
    private SensorEventListener mListener;
    private float mPrevDistance = 0;

    TextView mDistanceTextView;
    DistanceController mDistanceIndicator;

    HcSr04SensorRegister(Context context, SensorManager sensorManager,
                         MainUiController mainUiController, Activity activity) {
        super(context, sensorManager, mainUiController);
        mActivity = activity;

        mDistanceTextView = mActivity.findViewById(R.id.distance_text_view);
        mDistanceIndicator = new DistanceController(mDistanceTextView);
    }

    @Override
    public boolean isSensorEnabled() {
        return Features.MOTION_DETECTION_ENABLED &&
               Features.MOTION_DETECTION_PROXIMITY_ENABLED;
    }

    @Override
    public void start() {
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float currDistance;
                float prevDistance;
                synchronized (this) {
                    currDistance = event.values[0];
                    Log.d(TAG, "MotionDetector.onSensorChanged: " + currDistance);
                    if (currDistance < 0) {
                        return;
                    }

                    prevDistance = mPrevDistance;
                    mPrevDistance = currDistance;
                }
                if (Math.abs(currDistance - prevDistance) > 3) {
                    MotionDetectionEvent motionDetectionEvent = new MotionDetectionEvent();
                    motionDetectionEvent.mSource = MotionDetectionEvent.Source.PROXIMITY;
                    motionDetectionEvent.mProxmityParam = currDistance;
                    mDistanceIndicator.onDetected(motionDetectionEvent);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

        };
        mSensorManager.registerDynamicSensorCallback(
                new SensorManager.DynamicSensorCallback() {
                    @Override
                    public void onDynamicSensorConnected(Sensor sensor) {
                        if (sensor.getType() == sensor.TYPE_PROXIMITY) {
                            mSensorManager.registerListener(mListener, sensor,
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                    }
                });

        mDriver = new HcSr04SensorDriver();
        mDriver.registerSensor();

    }

    @Override
    public void shutdown() {
        mSensorManager.unregisterListener(mListener);
        mDriver.unregisterSensor();
        mDriver.close();
    }
}
