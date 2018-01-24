package com.android_things.sensor_experiment.controllers;

import android.app.Activity;
import android.widget.TextView;

import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorData;
import com.android_things.sensor_experiment.drivers.pms_7003.Pms7003SensorListener;
import com.android_things.sensor_experiment.pir.sensor_test.R;

import org.w3c.dom.Text;

/**
 * Created by lizhi on 1/24/18.
 */

public class MainUiController implements Pms7003SensorListener {
    private Activity mActivity;

    private TextView mPms7003View;

    public MainUiController(Activity activity) {
        mActivity = activity;

        mPms7003View = mActivity.findViewById(R.id.rgb_text_view);
    }

    @Override
    public void onPms7003SensorData(Pms7003SensorData data) {

    }
}
