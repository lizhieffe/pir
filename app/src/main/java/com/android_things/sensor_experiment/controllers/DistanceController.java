package com.android_things.sensor_experiment.controllers;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.detectors.MotionDetectionEvent;
import com.android_things.sensor_experiment.detectors.MotionDetectorListener;

/**
 * Created by lizhieffe on 1/4/18.
 */

public class DistanceController
        implements DetectionController, MotionDetectorListener {
    private TextView mDistanceView;

    public DistanceController(TextView distanceView) {
        mDistanceView = distanceView;
    }

    @Override
    public void start() {

    }

    @Override
    public void close() {

    }

    @Override
    public void onDetected(MotionDetectionEvent event) {
        if (event.mSource == MotionDetectionEvent.Source.PROXIMITY) {
            final double distance = event.mProxmityParam;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                   public void run() {
                       mDistanceView.setText(String.valueOf(distance));
                   }
               }
            );
        }
    }
}
