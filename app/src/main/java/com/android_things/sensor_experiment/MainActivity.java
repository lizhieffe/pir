package com.android_things.sensor_experiment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android_things.sensor_experiment.pir.sensor_test.R;
import com.android_things.sensor_experiment.sensors.MotionSensor;
import com.android_things.sensor_experiment.sensors.PirMotionSensor;
import com.android_things.sensor_experiment.sensors.ProximitySr04Sensor;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

public class MainActivity extends Activity {

    private PirMotionSensor motionSensor;
    private List<MotionSensor.Listener> motion_sensor_listeners;
    private List<DetectionIndicator> detection_indicators;
    private ProximitySr04Sensor proximity_sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("===lizhi", "111");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("===lizhi", "333");
        Button movement_indicator = findViewById(R.id.movement_indicator);
        UIDetectionIndicator ui_detection_indicator = new UIDetectionIndicator(
                getApplicationContext(), movement_indicator);
        LedDetectionIndicator led_detection_indicator = new LedDetectionIndicator();
        SensorDataRecorder sensorDataRecorder = new SensorDataRecorder(getApplicationContext());

        Log.e("===lizhi", "444");
        MotionSensor.Listener[] msl_array = {ui_detection_indicator, led_detection_indicator,
                sensorDataRecorder};
        motion_sensor_listeners = new ArrayList<>(Arrays.asList(msl_array));

        DetectionIndicator[] di_array = {ui_detection_indicator, led_detection_indicator};
        detection_indicators = new ArrayList<>(Arrays.asList(di_array));

        for (DetectionIndicator d : detection_indicators) {
            d.start();
        }


        proximity_sensor = new ProximitySr04Sensor();
        proximity_sensor.startup();
        Button measure_distance_btn = findViewById(R.id.measure_distance_btn);
        measure_distance_btn.setText("Measure Distance");
        measure_distance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ((Button) view).setText(
                            "Distance is " + proximity_sensor.readDistanceSync());
                } catch (IOException e) {
                    Log.e(TAG, "Cannot measure distance: ", e);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Cannot measure distance: ", e);
                }
            }
        });



        Log.e("===lizhi", "222");
        Gpio bus = openMotionSensorGpioBus();
        motionSensor = new PirMotionSensor(bus, motion_sensor_listeners);
        motionSensor.startup();
    }

    private Gpio openMotionSensorGpioBus() {
        Gpio bus;
        try {
            // BCM4 is the GPIO pin I have the sensor connected to on my raspberry pi
            bus = new PeripheralManagerService().openGpio("BCM4");
        } catch (IOException e) {
            throw new IllegalStateException("Can't open GPIO - can't create app.", e);
        }
        return bus;
    }

    @Override
    protected void onDestroy() {
        for (DetectionIndicator d : detection_indicators) {
            d.close();
        }
        motionSensor.shutdown();
        proximity_sensor.shutdown();
        super.onDestroy();
    }
}
