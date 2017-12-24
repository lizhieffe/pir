package com.example.android_things.pir.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements MotionSensor.Listener {

    private PirMotionSensor motionSensor;
    private Button movement_indicator;
    private volatile long last_movement_unix_time_ms = 0;
    private int movement_cool_down_ms = 500;
    private int movement_cool_down_error_ms = 50;
    private DetectionIndicatorController detection_indicator_controller;

    private Gpio ledBus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLedGpioBus();

        Button movement_indicator = findViewById(R.id.movement_indicator);
        detection_indicator_controller = new DetectionIndicatorController(
                getApplicationContext(), movement_indicator);

        Gpio bus = openMotionSensorGpioBus();
        List<MotionSensor.Listener> motion_sensor_listeners
                = new ArrayList<>(Arrays.asList(this, detection_indicator_controller));
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

    private void initLedGpioBus() {
        try {
            ledBus = new PeripheralManagerService().openGpio("BCM26");
            ledBus.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            throw new IllegalStateException("Can't open GPIO - can't create app.", e);
        }
    }

    @Override
    public void onMovement(Gpio gpio) {
        try {
            if (ledBus != null) {
                ledBus.setValue(gpio.getValue());
            }

            Log.d("lizhi===", "MOVEMENT DETECTED with GPIO value: " + gpio.getValue());
        } catch (IOException e) {
            throw new IllegalStateException("Can't get GPIO value: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        motionSensor.shutdown();
        if (ledBus != null) {
            try {
                ledBus.close();
            } catch (IOException e) {
                Log.e("===lizhi", "Error on PeripheralIO API", e);
            }
        }
        super.onDestroy();
    }
}
