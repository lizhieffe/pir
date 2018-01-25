package com.android_things.sensor_experiment.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by lizhieffe on 1/24/18.
 */

public abstract class Throttler<DataType> {
    private final static double DEFAULT_ONCE_EVERY_SECOND = 0.5;

    private long mLastUpdateMs = 0;

    private double mOnceEverySecond;

    public Throttler() {
        this(DEFAULT_ONCE_EVERY_SECOND);
    }

    public Throttler(double onceEverySecond) {
        mOnceEverySecond = onceEverySecond;
    }

    /** Consume the given data by using the {processData} method, with proper
     * throttling, on the current thread.
     * @param data: the data to throttle.
     */
    public void throttle(DataType data) {
        final long currTimeMs = System.currentTimeMillis();
        if (mLastUpdateMs == 0) {
            mLastUpdateMs = currTimeMs;
        }
        if (currTimeMs - mLastUpdateMs > mOnceEverySecond * 1000) {
            mLastUpdateMs = currTimeMs;
            processData(data);
        }
    }

    public void throttleOnNonUiThread(final DataType data) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                throttle(data);
            }
        });

    }

    public abstract void processData(DataType data);
}
