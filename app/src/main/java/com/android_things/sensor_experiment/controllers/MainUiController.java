package com.android_things.sensor_experiment.controllers;

import android.app.Activity;
import android.widget.TextView;

import com.zll.androidthings.drivers.bme_280_sensor.Bme280SensorListener;
import com.zll.androidthings.drivers.pms_7003.Pms7003SensorData;
import com.zll.androidthings.drivers.pms_7003.Pms7003SensorListener;
import com.zll.androidthings.drivers.tcs_34725.Color;
import com.zll.androidthings.drivers.tcs_34725.Tcs34725SensorListener;
import com.android_things.sensor_experiment.pir.sensor_test.R;
import com.android_things.sensor_experiment.utils.Throttler;

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

    private Throttler<Pms7003SensorData> mPms7003Updater;
    @Override
    public void onPms7003SensorData(Pms7003SensorData data) {
        if (mPms7003Updater == null) {
            mPms7003Updater = new Throttler<Pms7003SensorData>() {
                @Override
                public void processData(Pms7003SensorData data) {
                    mPms7003View.setText(data.toString());
                }
            };
        }
        mPms7003Updater.throttleOnNonUiThread(data);
    }

    private Throttler<Color> mTcs34725Updater;
    @Override
    public void onTcs34725SensorData(Color color) {
        if (mTcs34725Updater == null) {
            mTcs34725Updater = new Throttler<Color>() {
                @Override
                public void processData(Color data) {
                    mTcs34725View.setText(data.toString());
                }
            };
        }
        mTcs34725Updater.throttleOnNonUiThread(color);
    }

    private Throttler<Float> mTemperatureUpdater;
    @Override
    public void onTemperatureData(float temp) {
        if (mTemperatureUpdater == null) {
            mTemperatureUpdater = new Throttler<Float>() {
                @Override
                public void processData(Float data) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Temperature: ");
                    sb.append(String.format("%.4f", data));
                    sb.append(" degree in C");
                    mTemperatureView.setText(sb.toString());
                }
            };
        }
        mTemperatureUpdater.throttleOnNonUiThread(temp);
    }

    private Throttler<Float> mPressureUpdater;
    @Override
    public void onPressureData(final float pressure) {
        if (mPressureUpdater == null) {
            mPressureUpdater = new Throttler<Float>() {
                @Override
                public void processData(Float data) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Pressure: ");
                    sb.append(String.format("%.4f", pressure));
                    sb.append(" hPa");
                    mPressureView.setText(sb.toString());

                }
            };
        }
        mPressureUpdater.throttleOnNonUiThread(pressure);
    }

    private Throttler<Float> mHumidityUpdater;
    @Override
    public void onHumidityData(float data) {
        if (mHumidityUpdater == null) {
            mHumidityUpdater = new Throttler<Float>() {
                @Override
                public void processData(Float data) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Humidity: ");
                    sb.append(String.format("%.4f", data));
                    sb.append(" RH percentage");
                    mHumidityView.setText(sb.toString());

                }
            };
        }
        mHumidityUpdater.throttleOnNonUiThread(data);
    }
}
