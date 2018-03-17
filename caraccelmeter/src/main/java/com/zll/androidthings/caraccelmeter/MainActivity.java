package com.zll.androidthings.caraccelmeter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.zll.androidthings.drivers.mpu_6500_sensor.Mpu6500SensorAccelDriver;
import com.zll.androidthings.drivers.mpu_6500_sensor.Mpu6500SensorDriverFactory;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private View mDecorView;
    private AccelMeterView mView;

    private Mpu6500SensorAccelDriver mAccelSensorDriver;
    private Mpu6500SensorDriverFactory mAccelSensorDriverFactory;
    private SensorEventListener mListener;
    SensorManager mSensorManager;

    private AccelDataBank mAccelDataBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccelDataBank = new AccelDataBank();

        setUpView();
        setUpSensor();
    }

    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(mListener);
        mAccelSensorDriver.unregisterSensor();
        mAccelSensorDriver.close();

        super.onDestroy();
    }

    private void setUpView() {
        //set up notitle
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //set up full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // This is needed as well since the previous one to get full screen doesn't seem to work.
        mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        mView =  new AccelMeterView(MainActivity.this);
        setContentView(mView);
    }

    private void setUpSensor() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mAccelSensorDriverFactory = new Mpu6500SensorDriverFactory();
                mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

                mListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float[] data = new float[3];
                        for (int i = 0; i < data.length; i++) {
                            data[i] = event.values[i];
                        }
                        Log.d(TAG, "MainActivity.onSensorChanged: " + data[0] + " " + data[1] + " " + data[2]);
                        mAccelDataBank.addData(-data[0], data[1]);
                        mView.updateAccelData(mAccelDataBank.getData());
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        // Do nothing for now.
                    }
                };
                mSensorManager.registerDynamicSensorCallback(
                        new SensorManager.DynamicSensorCallback() {
                            @Override
                            public void onDynamicSensorConnected(Sensor sensor) {
                                if (sensor.getType() ==  Sensor.TYPE_ACCELEROMETER) {
                                    mSensorManager.registerListener(
                                            mListener, sensor,
                                            SensorManager.SENSOR_DELAY_NORMAL);
                                }
                            }
                        });

                mAccelSensorDriver = mAccelSensorDriverFactory.createAccelDriver();
                mAccelSensorDriver.registerSensor();
            }
        });
    }
}
