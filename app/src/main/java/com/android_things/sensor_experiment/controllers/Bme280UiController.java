package com.android_things.sensor_experiment.controllers;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.zll.androidthings.drivers.bme_280_sensor.Bme280SensorListener;

/**
 * Created by lizhieffe on 1/20/18.
 */

public class Bme280UiController implements Bme280SensorListener {
    private TextView mTemperatureView;
    private TextView mPressureView;
    private TextView mHumidityView;

    // Reading inside the delay will not be shown on the screen.
    private final static long DISPLAY_DELAY_MS = 500;
    private long mLastTemperatureDisplayUpldateMs;
    private long mLastPressureDisplayUpldateMs;
    private long mLastHumidityDisplayUpldateMs;

    public Bme280UiController(TextView temperatureView,
                              TextView pressureView,
                              TextView humidityView) {
        mTemperatureView = temperatureView;
        mPressureView = pressureView;
        mHumidityView = humidityView;
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
