package com.android_things.sensor_experiment;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android_things.sensor_experiment.detectors.AirQualityDetector;
import com.android_things.sensor_experiment.detectors.AmbientLightDetector;
import com.android_things.sensor_experiment.indicator.AmbientLightIlluminanceIdicator;
import com.android_things.sensor_experiment.indicator.DetectionIndicator;
import com.android_things.sensor_experiment.indicator.LedDetectorIndicator;
import com.android_things.sensor_experiment.indicator.UIDetectorIndicator;
import com.android_things.sensor_experiment.detectors.MotionDetector;
import com.android_things.sensor_experiment.pir.sensor_test.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

public class MainActivity extends Activity {

    private List<DetectionIndicator> detection_indicators;

    private MotionDetector mMotionDetector;
    private AmbientLightDetector mAmbientLightDetector;
    private AirQualityDetector mAirQualityDectector;

    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

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

        mMotionDetector = new MotionDetector();
        mMotionDetector.start();
        mMotionDetector.addListener(ui_detection_indicator);
        mMotionDetector.addListener(led_detection_indicator);
        mMotionDetector.addListener(sensorDataRecorder);

        mAmbientLightDetector = new AmbientLightDetector(mSensorManager);
        mAmbientLightDetector.addListener(new AmbientLightIlluminanceIdicator(
                 (TextView)findViewById(R.id.ambient_light_value_text_view)));
        mAmbientLightDetector.start();

        try {
            mAirQualityDectector = new AirQualityDetector(mSensorManager);
            mAirQualityDectector.start();
        } catch (IOException e) {
            Log.e(TAG, "MainActivity.onCreate: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        for (DetectionIndicator d : detection_indicators) {
            d.close();
        }
        mMotionDetector.shutdown();
        mAmbientLightDetector.shutdown();
        mAirQualityDectector.shutdown();
        super.onDestroy();
    }
}
