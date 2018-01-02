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
import com.android_things.sensor_experiment.sensors.zx_gesture.ZxGestureSensor;
import com.android_things.sensor_experiment.utils.ByteUtil;

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

    ZxGestureSensor mZxGestureSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        maybeStartMotionDetection();
        maybeStartAmbientLightDetection();
        maybeStartAirQualityDetection();

        if (Features.GESTURE_DETECTION_ENABLED) {
            try {
                mZxGestureSensor = new ZxGestureSensor();
                mZxGestureSensor.startup();
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            byte[] position = mZxGestureSensor.readPositions();
                            Log.d(TAG, "MainActivity.run: position = ["
                                    + ByteUtil.byteToBinaryString(position[0])
                                    + ", " + ByteUtil.byteToBinaryString(position[1]) + "]");
                            Log.d(TAG, "MainActivity.run: position = ["
                                    + ByteUtil.byteToUnsignedInt(position[0])
                                    + ", " + ByteUtil.byteToUnsignedInt(position[1]) + "]");
                            try {
                                Thread.sleep(25);
                            } catch (InterruptedException e) {

                            }
                        }
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "MainActivity.onCreate: ", e);
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
            mZxGestureSensor.shutdown();
        }

        super.onDestroy();
    }

    private void maybeStartAirQualityDetection() {
        if (Features.AIR_QUALITRY_DETECTION_ENABLED) {
            try {
                mAirQualityDetector = new AirQualityDetector(mSensorManager);
                mAirQualityDetector.start();
            } catch (IOException e) {
                Log.e(TAG, "MainActivity.onCreate: ", e);
            }
        }

    }

    private void maybeStartAmbientLightDetection() {
        if (Features.AMBIENT_LIGHT_DETECTION_ENABLED) {
            mAmbientLightDetector = new AmbientLightDetector(mSensorManager);
            mAmbientLightDetector.addListener(new AmbientLightIlluminanceIdicator(
                    (TextView) findViewById(R.id.ambient_light_value_text_view)));
            mAmbientLightDetector.start();
        }

    }

    private void maybeStartMotionDetection() {
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
    }
}
