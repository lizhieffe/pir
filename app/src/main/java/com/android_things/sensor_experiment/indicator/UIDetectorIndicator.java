package com.android_things.sensor_experiment.indicator;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import com.android_things.sensor_experiment.motion.MotionDetectorListener;
import com.android_things.sensor_experiment.pir.sensor_test.R;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 12/23/17.
 */

public class UIDetectorIndicator
        implements MotionDetectorListener, DetectionIndicator {
    private Context context;
    private Button movement_indicator;

    private long last_indication_unix_time_ms = 0;
    final private int indication_cool_down_ms = 500;
    final private int indication_cool_down_error_ms = 50;

    public UIDetectorIndicator(Context context, Button movement_indicator) {
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
    synchronized public void onDetected() {
        if (System.currentTimeMillis() - last_indication_unix_time_ms
                >= indication_cool_down_ms) {
            setMovementDetected();
            last_indication_unix_time_ms = System.currentTimeMillis();
            setNoMovementDetected();
        }
    }

    enum IndicatorUpdateType {
        MOVEMENT,
        NO_MOVEMENT
    }


    private class IndicatorUpdateParams {
        IndicatorUpdateParams(IndicatorUpdateType type, long delayMs) {
            mType = type;
            mDelayMs = delayMs;
        }

        IndicatorUpdateType mType;
        long mDelayMs;
    }

    private class SetMovementDetectedTask
            extends AsyncTask<IndicatorUpdateParams, Void, Void> {
        private IndicatorUpdateParams mParams;

        @Override
        protected Void doInBackground(IndicatorUpdateParams... params) {
            mParams = params[0];
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mParams.mDelayMs > 0) {
                try {
                    Thread.sleep(mParams.mDelayMs);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Cannot sleep: ", e);
                }
            }
            if (mParams.mType == IndicatorUpdateType.MOVEMENT) {
                movement_indicator.setBackgroundColor(
                        context.getResources().getColor(R.color.red));
                movement_indicator.setText("MOVEMENT DETECTED!!!");
            } else if (mParams.mType == IndicatorUpdateType.NO_MOVEMENT){
                movement_indicator.setBackgroundColor(context.getResources().getColor(R.color.darkgreen));
                movement_indicator.setText("No Movement");
            }
        }
    }

    private void setMovementDetected() {
        IndicatorUpdateParams params = new IndicatorUpdateParams(
                IndicatorUpdateType.MOVEMENT,0);
        new SetMovementDetectedTask().execute(params);
    }

    private void setNoMovementDetected() {
        IndicatorUpdateParams params = new IndicatorUpdateParams(
                IndicatorUpdateType.NO_MOVEMENT,
                indication_cool_down_ms - indication_cool_down_error_ms);
        new SetMovementDetectedTask().execute(params);
    }
}
