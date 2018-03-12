package com.zll.androidthings.caraccelmeter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * Created by lizhieffe on 3/11/18.
 */

public class AccelMeterView extends View {
    private static final String TAG = "Accel Meter View";
    private static final double MAX_G_IN_DISPLAY = 1.05;
    private static final int SCREEN_MARGIN_PIXELS = 10;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenXCenter;
    private int mScreenYCenter;

    private int mRadius_0_25_G;
    private int mRadius_0_50_G;
    private int mRadius_0_75_G;
    private int mRadius_1_00_G;

    // Acceleration ahead.
    private double mAhead;
    // Acceleration to the right side.
    private double mRight;

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
        mRadius_0_25_G = (int)((screenOutmostRadius - SCREEN_MARGIN_PIXELS) / MAX_G_IN_DISPLAY  * 0.25);
        mRadius_0_50_G = (int)((screenOutmostRadius - SCREEN_MARGIN_PIXELS) / MAX_G_IN_DISPLAY  * 0.50);
        mRadius_0_75_G = (int)((screenOutmostRadius - SCREEN_MARGIN_PIXELS) / MAX_G_IN_DISPLAY  * 0.75);
        mRadius_1_00_G = (int)((screenOutmostRadius - SCREEN_MARGIN_PIXELS) / MAX_G_IN_DISPLAY  * 1.00);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLACK);
        canvas.drawCircle(mScreenXCenter, mScreenYCenter, mRadius_0_25_G, p);

        p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLACK);
        canvas.drawCircle(mScreenXCenter, mScreenYCenter, mRadius_0_50_G, p);

        p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLACK);
        canvas.drawCircle(mScreenXCenter, mScreenYCenter, mRadius_0_75_G, p);

        p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLACK);
        canvas.drawCircle(mScreenXCenter, mScreenYCenter, mRadius_1_00_G, p);

        p = new Paint();
        p.setStyle(Paint.Style.FILL);
        double xPosition = mScreenXCenter + mScreenXCenter * mRight;
        double yPosition = mScreenYCenter + mScreenYCenter * mAhead;
        canvas.drawCircle((float)xPosition, (float)yPosition, 50, p);
        Log.d(TAG, "AccelMeterView.onDraw: x = " + xPosition + " y = " + yPosition);
    }

    synchronized public void updateAccelValues(double ahead, double right) {
        mAhead = ahead;
        mRight = right;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }
    }
}
