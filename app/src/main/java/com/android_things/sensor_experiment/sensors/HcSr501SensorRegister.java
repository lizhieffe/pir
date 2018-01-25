package com.android_things.sensor_experiment.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.widget.Button;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.controllers.LedDetectorController;
import com.android_things.sensor_experiment.controllers.MainUiController;
import com.android_things.sensor_experiment.controllers.UIDetectorController;
import com.android_things.sensor_experiment.detectors.MotionDetectionEvent;
import com.android_things.sensor_experiment.detectors.MotionDetectorListener;
import com.android_things.sensor_experiment.drivers.hc_sr501_sensor.HcSr501Sensor;
import com.android_things.sensor_experiment.drivers.hc_sr501_sensor.HcSr501SensorDriver;
import com.android_things.sensor_experiment.logger.MotionLogger;
import com.android_things.sensor_experiment.pir.sensor_test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizhieffe on 1/24/18.
 */

public class HcSr501SensorRegister extends SensorRegisterBase {
    // TODO: Refactor the get UI part out of this class.
    private Activity mActivity;

    private HcSr501SensorDriver mDriver;
    private TriggerEventListener mListener;
    private Sensor mPirSensor;
    private long mPirDelayStarts;
    private List<MotionDetectorListener> mPirListener;

    private LedDetectorController mLedDetectionIndicator;
    private MotionLogger mSensorDataRecorder;

    HcSr501SensorRegister(Context context, SensorManager sensorManager,
                          MainUiController mainUiController, Activity activity) {
        super(context, sensorManager, mainUiController);
        mActivity = activity;

        mPirListener = new ArrayList<>();

        Button movement_indicator = mActivity.findViewById(R.id.movement_indicator);
        UIDetectorController ui_detection_indicator = new UIDetectorController(
                mContext, movement_indicator);
        mLedDetectionIndicator = new LedDetectorController();
        mSensorDataRecorder = new MotionLogger(mContext);
        mPirListener.add(ui_detection_indicator);
        mPirListener.add(mLedDetectionIndicator);
        mPirListener.add(mSensorDataRecorder);
    }

    @Override
    public boolean isSensorEnabled() {
        return Features.MOTION_DETECTION_ENABLED &&
               Features.MOTION_DETECTION_PIR_ENABLED;
    }

    @Override
    public void start() {
        mListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                if (event.values[0] > 0
                        && System.currentTimeMillis() - mPirDelayStarts > HcSr501Sensor.DELAY_TIME_MS) {
                    mPirDelayStarts = System.currentTimeMillis();
                    MotionDetectionEvent motionDetectionEvent
                            = new MotionDetectionEvent();
                    motionDetectionEvent.mSource
                            = MotionDetectionEvent.Source.PIR;
                    notifyListeners(motionDetectionEvent);
                }
                mSensorManager.requestTriggerSensor(mListener, mPirSensor);

            }
        };
        mSensorManager.registerDynamicSensorCallback(
            new SensorManager.DynamicSensorCallback() {
                @Override
                public void onDynamicSensorConnected(Sensor sensor) {
                    if (sensor.getType() == sensor.TYPE_MOTION_DETECT) {
                        mSensorManager.requestTriggerSensor(mListener, sensor);
                        mPirSensor = sensor;
                    }
                }
            });
        mDriver = new HcSr501SensorDriver();
        mDriver.registerSensor();

        mLedDetectionIndicator.start();
    }

    @Override
    public void shutdown() {
        mLedDetectionIndicator.close();

        mSensorManager.cancelTriggerSensor(mListener, mPirSensor);
        mDriver.unregisterSensor();
        mDriver.close();

        mPirListener.clear();
        mPirListener = null;
    }

    synchronized void notifyListeners(MotionDetectionEvent event) {
        assert (event.mSource == MotionDetectionEvent.Source.PIR);

        for (MotionDetectorListener listener : mPirListener) {
            listener.onDetected(event);
        }
    }
}
