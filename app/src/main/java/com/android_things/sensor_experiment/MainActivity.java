package com.android_things.sensor_experiment;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.detectors.AirQualityDetector;
import com.android_things.sensor_experiment.detectors.AmbientLightDetector;
import com.android_things.sensor_experiment.indicator.AmbientLightIlluminanceIdicator;
import com.android_things.sensor_experiment.indicator.DetectionIndicator;
import com.android_things.sensor_experiment.indicator.LedDetectorIndicator;
import com.android_things.sensor_experiment.indicator.UIDetectorIndicator;
import com.android_things.sensor_experiment.detectors.MotionDetector;
import com.android_things.sensor_experiment.pir.sensor_test.R;
import com.google.android.things.contrib.driver.zxgesturesensor.ZXGestureSensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

public class MainActivity extends Activity {

    private List<DetectionIndicator> detection_indicators;

    private MotionDetector mMotionDetector;
    private AmbientLightDetector mAmbientLightDetector;
    private AirQualityDetector mAirQualityDetector;

    private SensorManager mSensorManager;

    ZXGestureSensor mZxGestureSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        if (Features.MOTION_DETECTION_ENABLED) {
            Button movement_indicator = findViewById(R.id.movement_indicator);
            UIDetectorIndicator ui_detection_indicator = new UIDetectorIndicator(
                    getApplicationContext(), movement_indicator);
            LedDetectorIndicator led_detection_indicator = new LedDetectorIndicator();
            SensorDataRecorder sensorDataRecorder = new SensorDataRecorder(getApplicationContext());

            DetectionIndicator[] di_array = {ui_detection_indicator, led_detection_indicator};
            detection_indicators = new ArrayList<>(Arrays.asList(di_array));

            for (DetectionIndicator d : detection_indicators) {
                d.start();
            }

            mMotionDetector = new MotionDetector(mSensorManager);
            mMotionDetector.start();
            mMotionDetector.addListener(ui_detection_indicator);
            mMotionDetector.addListener(led_detection_indicator);
            mMotionDetector.addListener(sensorDataRecorder);
        }

        if (Features.AMBIENT_LIGHT_DETECTION_ENABLED) {
            mAmbientLightDetector = new AmbientLightDetector(mSensorManager);
            mAmbientLightDetector.addListener(new AmbientLightIlluminanceIdicator(
                    (TextView) findViewById(R.id.ambient_light_value_text_view)));
            mAmbientLightDetector.start();
        }

        if (Features.AIR_QUALITRY_DETECTION_ENABLED) {
            try {
                mAirQualityDetector = new AirQualityDetector(mSensorManager);
                mAirQualityDetector.start();
            } catch (IOException e) {
                Log.e(TAG, "MainActivity.onCreate: ", e);
            }
        }

        if (Features.GESTURE_DETECTION_ENABLED) {
            try {
                mZxGestureSensor = ZXGestureSensor.getI2cSensor("I2C1", new Handler());
                mZxGestureSensor.setListener(new ZXGestureSensor.OnGestureEventListener() {
                    @Override
                    public void onGestureEvent(ZXGestureSensor sensor, ZXGestureSensor.Gesture gesture,
                                               int param) {
                        Log.d(TAG, "MainActivity.onGestureEvent: on gesture" + gesture.toString());
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "MainActivity.onCreate: ", e);
            }

            for (int i = 0; i < 20; i++) {
                Log.d(TAG, "MainActivity.onCreate: " + mZxGestureSensor.getGestureDetector().getXpos());
                Log.d(TAG, "MainActivity.onCreate: " + mZxGestureSensor.getGestureDetector().getZpos());
                Log.d(TAG, "MainActivity.onCreate: " + mZxGestureSensor.getGestureDetector().getGesture());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (Features.MOTION_DETECTION_ENABLED) {
            for (DetectionIndicator d : detection_indicators) {
                d.close();
            }
            mMotionDetector.shutdown();
        }
        if (Features.AMBIENT_LIGHT_DETECTION_ENABLED) {
            mAmbientLightDetector.shutdown();
        }
        if (Features.AIR_QUALITRY_DETECTION_ENABLED) {
            mAirQualityDetector.shutdown();
        }
        if (Features.GESTURE_DETECTION_ENABLED) {
            try {
                mZxGestureSensor.close();
            } catch (IOException e) {
                Log.e(TAG, "MainActivity.onDestroy: ", e);
            }
        }

        super.onDestroy();
    }
}
