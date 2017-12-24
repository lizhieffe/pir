package com.example.android_things.pir.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import com.google.android.things.pio.Gpio;
import java.io.IOException;

/**
 * Created by lizhieffe on 12/23/17.
 */

class UIDetectionIndicator implements MotionSensor.Listener, DetectionIndicator {
    private Context context;
    private Button movement_indicator;

    private long last_indication_unix_time_ms = 0;
    final private int indication_cool_down_ms = 500;
    final private int indication_cool_down_error_ms = 50;

    UIDetectionIndicator(Context context, Button movement_indicator) {
        this.context = context;
        this.movement_indicator = movement_indicator;
    }

    @Override
    public void start() {
        setNoMovementDetected();
    }

    @Override
    public void close() {
        setMovementDetected();
    }

    @Override
    public void onMovement(Gpio gpio) {
        try {
            if (gpio.getValue() == true) {
                if (System.currentTimeMillis() - last_indication_unix_time_ms
                        >= indication_cool_down_ms) {
                    setMovementDetected();
                    last_indication_unix_time_ms = System.currentTimeMillis();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            setNoMovementDetected();
                        }
                    }, indication_cool_down_ms - indication_cool_down_error_ms);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't get GPIO value: ", e);
        }
    }

    private void setNoMovementDetected() {
        movement_indicator.setBackgroundColor(context.getResources().getColor(R.color.darkgreen));
        movement_indicator.setText("No Movement");
    }

    private void setMovementDetected() {
        movement_indicator.setBackgroundColor(
                context.getResources().getColor(R.color.red));
        movement_indicator.setText("MOVEMENT DETECTED!!!");
    }
}
