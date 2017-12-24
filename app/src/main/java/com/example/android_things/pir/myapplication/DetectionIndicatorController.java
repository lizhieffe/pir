package com.example.android_things.pir.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import com.google.android.things.pio.Gpio;
import java.io.IOException;

/**
 * Created by lizhieffe on 12/23/17.
 */

class DetectionIndicatorController implements MotionSensor.Listener {
    private Context context;
    private Button movement_indicator;

    DetectionIndicatorController (Context context, Button movement_indicator) {
        this.context = context;
        this.movement_indicator = movement_indicator;
        init();
    }

    @Override
    public void onMovement(Gpio gpio) {
        try {
            if (gpio.getValue() == true) {
                movement_indicator.setBackgroundColor(context.getResources().getColor(R.color.red));
                movement_indicator.setText("MOVEMENT DETECTED!!!");
            } else {
                init();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't get GPIO value: ", e);
        }
    }

    private void init() {
        movement_indicator.setBackgroundColor(context.getResources().getColor(R.color.darkgreen));
        movement_indicator.setText("No Movement");
    }
}
