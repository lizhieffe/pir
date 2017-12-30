package com.android_things.sensor_experiment;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.android_things.sensor_experiment.indicator.DetectionIndicator;
import com.android_things.sensor_experiment.indicator.LedDetectorIndicator;
import com.android_things.sensor_experiment.indicator.UIDetectorIndicator;
import com.android_things.sensor_experiment.motion.MotionDetector;
import com.android_things.sensor_experiment.pir.sensor_test.R;
import com.android_things.sensor_experiment.sensors.AmbientLightSen14350Sensor;
import com.android_things.sensor_experiment.sensors.AmbientLightSen14350SensorDriver;
import com.android_things.sensor_experiment.sensors.Ccs811Sensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

public class MainActivity extends Activity {

    private List<DetectionIndicator> detection_indicators;

    private MotionDetector mMotionDetector;

    // private AmbientLightSen14350Sensor mAmbientLightSensor;
    private AmbientLightSen14350SensorDriver mAmbientLightSensorDriver;
    private SensorManager mSensorManager;
    private SensorEventListener mListener;
    private Ccs811Sensor mCcs811Sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "MainActivity.onCreate: ================================");
        Log.d(TAG, "MainActivity.onCreate: ================================");
        Log.d(TAG, "MainActivity.onCreate: ================================");
        Log.d(TAG, "MainActivity.onCreate: ================================");
        Log.d(TAG, "MainActivity.onCreate: ================================");
        Log.d(TAG, "MainActivity.onCreate: ================================");
        Log.d(TAG, "MainActivity.onCreate: ================================");
        Log.d(TAG, "MainActivity.onCreate: ================================");
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

        // mAmbientLightSensor = new AmbientLightSen14350Sensor();
        // try {
        //     mAmbientLightSensor.startup();
        // } catch (IOException e) {
        //     Log.e(TAG, "MainActivity.onCreate: cannot startup the ambient light sensor", e);
        // }

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                // Log.d(TAG, "MainActivity.onSensorChanged: " + sensorEvent.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                // Log.d(TAG, "MainActivity.onAccuracyChanged: value = " +i +", " + sensor.toString());
            }
        };
        Log.d(TAG, "MainActivity.onDynamicSensorConnected: 000000000");
        mSensorManager.registerDynamicSensorCallback(new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                Log.d(TAG, "MainActivity.onDynamicSensorConnected: aaaaaaaa");
                if (sensor.getType() ==  Sensor.TYPE_LIGHT) {
                    Log.d(TAG, "MainActivity.onDynamicSensorConnected: bbbbbbbbbbb");
                    mSensorManager.registerListener(mListener, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        mAmbientLightSensorDriver = new AmbientLightSen14350SensorDriver();
        mAmbientLightSensorDriver.registerSensor();

        // for (int i = 0; i < 100; i++) {
        //     try {
        //         Log.d(TAG, "MainActivity.onCreate: 111111");
        //         Log.d(TAG, "MainActivity.onCreate: lux = " + mAmbientLightSensorDriver.getDevice().readLuxLevel());
        //         Thread.sleep(1000);
        //     } catch (Exception e) {

        //     }
        // }

        try {
            mCcs811Sensor = new Ccs811Sensor();
            mCcs811Sensor.setMode(Ccs811Sensor.MODE_60S);
        } catch (IOException e) {
            Log.e(TAG, "MainActivity.onCreate: cs811: ", e);
        }


        try {
            int[] values = mCcs811Sensor.readAlgorithmResults();
            Log.d(TAG, "MainActivity.onCreate: values size = " + values.length);
            Log.d(TAG, "MainActivity.onCreate: values[0] = " + values[0]);
            Log.d(TAG, "MainActivity.onCreate: values[1] = " + values[1]);
            Log.d(TAG, "MainActivity.onCreate: values[2] = " + values[2]);
            Log.d(TAG, "MainActivity.onCreate: values[3] = " + values[3]);
        } catch (IOException e) {
            Log.e(TAG, "MainActivity.onCreate: cs811: ", e);
        }

        try {
            mCcs811Sensor.close();
        } catch (Exception e) {
            Log.e(TAG, "MainActivity.onCreate: cs811: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        for (DetectionIndicator d : detection_indicators) {
            d.close();
        }
        mMotionDetector.shutdown();
        // mAmbientLightSensor.shutdown();
        mSensorManager.unregisterListener(mListener);
        mAmbientLightSensorDriver.unregisterSensor();
        super.onDestroy();
    }
}
