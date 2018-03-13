package com.zll.androidthings.caraccelmeter;

/**
 * Created by lizhieffe on 3/12/18.
 */

public class AccelData {
    float ahead;
    float right;
    long timestamp_ms;

    public AccelData(float x, float y, long timestamp_ms) {
        this.ahead = x;
        this.right = y;
        this.timestamp_ms = timestamp_ms;
    }
}
