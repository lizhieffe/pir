package com.android_things.sensor_experiment.controllers;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android_things.sensor_experiment.detectors.MotionDetectionEvent;
import com.android_things.sensor_experiment.detectors.MotionDetectorListener;
import com.android_things.sensor_experiment.drivers.bme_280_sensor.Bme280SensorListener;
import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorData;
import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorListener;
import com.android_things.sensor_experiment.drivers.tcs_34725.Color;
import com.android_things.sensor_experiment.drivers.tcs_34725.Tcs34725SensorDriver;
import com.android_things.sensor_experiment.drivers.tcs_34725.Tcs34725SensorListener;
import com.android_things.sensor_experiment.logger.Bme280SensorLogger;
import com.android_things.sensor_experiment.pir.sensor_test.R;

/**
 * Created by lizhi on 1/24/18.
 */

public class MainUiController implements
        Bme280SensorListener, Pms7003SensorListener, Tcs34725SensorListener {
    private Activity mActivity;

    // Reading inside the delay will not be shown on the screen.
    private final static long DISPLAY_DELAY_MS = 500;

    private TextView mTemperatureView;
    private TextView mPressureView;
    private TextView mHumidityView;
    private long mLastTemperatureDisplayUpldateMs;
    private long mLastPressureDisplayUpldateMs;
    private long mLastHumidityDisplayUpldateMs;

    private TextView mPms7003View;
    private long mLastDisplayUpdatePms7003Ms;

    private TextView mTcs34725View;
    private long mLastDisplayUpdateTcs34725Ms;

    public MainUiController(Activity activity) {
        mActivity = activity;

        mTemperatureView = mActivity.findViewById(R.id.temperature_text_view);
        mPressureView = mActivity.findViewById(R.id.pressure_text_view);
        mHumidityView = mActivity.findViewById(R.id.humidity_text_view);

        mPms7003View = mActivity.findViewById(R.id.air_quality_text_view);
        mTcs34725View = mActivity.findViewById(R.id.rgb_text_view);
    }

    @Override
    public void onPms7003SensorData(Pms7003SensorData data) {
        final Pms7003SensorData localData = data;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastDisplayUpdatePms7003Ms > DISPLAY_DELAY_MS) {
                    mPms7003View.setText(localData.toString());
                    mLastDisplayUpdatePms7003Ms = currTimeMs;
                }
            }
        });
    }

    @Override
    public void onTcs34725SensorData(Color color) {
        final Color localData = color;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastDisplayUpdateTcs34725Ms > DISPLAY_DELAY_MS) {
                    mTcs34725View.setText(localData.toString());
                    mLastDisplayUpdateTcs34725Ms = currTimeMs;
                }
            }
        });
    }

    @Override
    public void onTemperatureData(float temp) {
        final float localTemp = temp;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastTemperatureDisplayUpldateMs > DISPLAY_DELAY_MS) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("Temperature: ");
                    sb.append(String.format("%.4f", localTemp));
                    sb.append(" degree in C");

                    mTemperatureView.setText(sb.toString());
                    mLastTemperatureDisplayUpldateMs = currTimeMs;
                }
            }
        });
    }

    @Override
    public void onPressureData(float pressure) {
        final float localPressure = pressure;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastPressureDisplayUpldateMs > DISPLAY_DELAY_MS) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("Pressure: ");
                    sb.append(String.format("%.4f", localPressure));
                    sb.append(" hPa");

                    mPressureView.setText(sb.toString());
                    mLastPressureDisplayUpldateMs = currTimeMs;
                }
            }
        });
    }

    @Override
    public void onHumidityData(float data) {
        final float localData = data;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final long currTimeMs = System.currentTimeMillis();
                if (currTimeMs - mLastHumidityDisplayUpldateMs > DISPLAY_DELAY_MS) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("Humidity: ");
                    sb.append(String.format("%.4f", localData));
                    sb.append(" RH percentage");

                    mHumidityView.setText(sb.toString());
                    mLastHumidityDisplayUpldateMs = currTimeMs;
                }
            }
        });
    }
}
