package com.android_things.sensor_experiment.indicator;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.detectors.AmbientLightListener;

import java.io.IOException;

/**
 * Created by lizhieffe on 12/30/17.
 */

public class AmbientLightIlluminanceIdicator
        implements DetectionIndicator, AmbientLightListener {
    private TextView mIlluminanceView;

    public AmbientLightIlluminanceIdicator(TextView illuminanceView) {
        mIlluminanceView = illuminanceView;
    }

    @Override
    public void start() {

    }

    @Override
    public void close() {

    }

    @Override
    public void onDetected(float lux) {
        final float finalLux = lux;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append("Ambient Light Illuminance: ");
                sb.append(finalLux);
                sb.append(" lux.");
                mIlluminanceView.setText(sb.toString());
            }
        });

    }
}
