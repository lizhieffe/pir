package com.android_things.sensor_experiment.indicator;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.drivers.mpu_6500.Mpu6500SensorListener;

/**
 * Created by lizhi on 1/12/18.
 */

public class AccelerometerUiController implements Mpu6500SensorListener {
    private TextView mAccelerometerView;

    public AccelerometerUiController(TextView accelerometerView) {
        mAccelerometerView = accelerometerView;
    }

    @Override
    public void onAccelData(float[] data) {
        final float[] finalData = data;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append("accel read [x, y, z]: ");
                sb.append(finalData[0]);
                sb.append(" ");
                sb.append(finalData[1]);
                sb.append(" ");
                sb.append(finalData[2]);
                mAccelerometerView.setText(sb.toString());
            }
        });
    }
}
