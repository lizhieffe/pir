package com.zll.androidthings.caraccelmeter;

import android.graphics.Canvas;

/**
 * Created by lizhieffe on 3/16/18.
 */

public class CorneringGPloter {
    private static final int AXIS_MARGIN = 20;

    private Canvas mCanvas;
    private int mBottom, mTop, mLeft, mRight;

    private int mTopAxis, mLeftAxis;

    public CorneringGPloter(Canvas canvas, int bottom, int top, int left, int right) {
        mCanvas = canvas;
        mBottom = bottom;
        mTop = top;
        mLeft = left;
        mRight = right;

        mTopAxis = mTop + AXIS_MARGIN;
        mLeftAxis = mLeft + AXIS_MARGIN;
    }

    private void plotBackground() {

    }
}
