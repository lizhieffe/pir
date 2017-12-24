package com.example.android_things.pir.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private PirMotionSensor motionSensor;
    private List<MotionSensor.Listener> motion_sensor_listeners;
    private List<DetectionIndicator> detection_indicators;

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

        Log.e("===lizhi", "444");
        MotionSensor.Listener[] msl_array = {ui_detection_indicator, led_detection_indicator};
        motion_sensor_listeners = new ArrayList<>(Arrays.asList(msl_array));

        DetectionIndicator[] di_array = {ui_detection_indicator, led_detection_indicator};
        detection_indicators = new ArrayList<>(Arrays.asList(di_array));

        for (DetectionIndicator d : detection_indicators) {
            d.start();
        }

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
        super.onDestroy();
    }
}
