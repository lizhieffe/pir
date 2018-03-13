package com.zll.androidthings.caraccelmeter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.List;

/**
 * Created by lizhieffe on 3/11/18.
 */

public class AccelMeterView extends View {
    private static final String TAG = "Accel Meter View";
    private static final double MAX_G_IN_DISPLAY = 1.20;
    private static final int SCREEN_MARGIN_PIXELS = 10;

    private static final int CURR_INDICATOR_RADIUS = 16;
    private static final int PAST_INDICATOR_RADIUS = 5;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenXCenter;
    private int mScreenYCenter;

    private int mPixelsPerG;
    private int mRadius_0_50_G;
    private int mRadius_1_00_G;

    // Acceleration ahead.
    private double mAhead;
    // Acceleration to the right side.
    private double mRight;
    private List<AccelData> mAccelData;

    private Context mContext;

    public AccelMeterView(Context context) {
        super(context);
        mContext = context;

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenXCenter = mScreenWidth / 2;
        mScreenYCenter = mScreenHeight / 2;

        int screenOutmostRadius = Math.min(mScreenHeight / 2, mScreenHeight / 2);
        mPixelsPerG = (int)((screenOutmostRadius - SCREEN_MARGIN_PIXELS) / MAX_G_IN_DISPLAY);
        mRadius_0_50_G = (int)(mPixelsPerG * 0.50);
        mRadius_1_00_G = (int)(mPixelsPerG * 1.00);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        plotBackground(canvas);
        if (mAccelData != null) {
            for (int i = 0; i < mAccelData.size(); i++) {
                plotSingleAccelData(mAccelData.get(i), i == mAccelData.size() - 1, canvas);
            }
        }
    }

    synchronized public void updateAccelData(List<AccelData> data) {
        mAccelData = data;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }
    }

    private void plotSingleAccelData(AccelData data, boolean isLatestData, Canvas canvas) {
        int color;
        int radius;
        Paint.Style style;
        if (isLatestData) {
            color = Color.RED;
            radius = CURR_INDICATOR_RADIUS;
            style = Paint.Style.FILL;
        } else {
            color = Color.YELLOW;
            radius = PAST_INDICATOR_RADIUS;
            style = Paint.Style.STROKE;
        }

        Paint p;
        p = new Paint();
        p.setColor(color);
        p.setStyle(style);
        double xPosition = mScreenXCenter + mPixelsPerG * data.right;
        double yPosition = mScreenYCenter + mPixelsPerG * data.ahead;
        canvas.drawCircle((float)xPosition, (float)yPosition, radius, p);
    }

    private void plotBackground(Canvas canvas) {
        setBackgroundColor(Color.BLACK);

        Paint p;

        p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        canvas.drawCircle(mScreenXCenter, mScreenYCenter, mRadius_0_50_G, p);

        p = new Paint();
        p.setColor(Color.WHITE);
        p.setTextSize(20);
        canvas.drawText("0.5 G", mScreenXCenter, mScreenYCenter - mRadius_0_50_G, p);

        p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        canvas.drawCircle(mScreenXCenter, mScreenYCenter, mRadius_1_00_G, p);

        p = new Paint();
        p.setColor(Color.WHITE);
        p.setTextSize(20);
        canvas.drawText("1.0 G", mScreenXCenter, mScreenYCenter - mRadius_1_00_G, p);

        // Vertical line.
        p = new Paint();
        p.setColor(Color.YELLOW);
        canvas.drawLine(mScreenXCenter, 0, mScreenXCenter, mScreenHeight, p);

        // Horizontal line.
        p = new Paint();
        p.setColor(Color.YELLOW);
        canvas.drawLine(0, mScreenYCenter, mScreenWidth, mScreenYCenter, p);
    }
}
