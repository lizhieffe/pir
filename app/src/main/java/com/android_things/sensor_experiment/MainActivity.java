package com.android_things.sensor_experiment;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.android_things.sensor_experiment.indicator.DetectionIndicator;
import com.android_things.sensor_experiment.indicator.LedDetectorIndicator;
import com.android_things.sensor_experiment.indicator.UIDetectorIndicator;
import com.android_things.sensor_experiment.motion.MotionDetector;
import com.android_things.sensor_experiment.pir.sensor_test.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private List<DetectionIndicator> detection_indicators;

    private MotionDetector mMotionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    @Override
    protected void onDestroy() {
        for (DetectionIndicator d : detection_indicators) {
            d.close();
        }
        // proximity_sensor.shutdown();
        mMotionDetector.shutdown();
        super.onDestroy();
    }
}
