package com.zll.androidthings.caraccelmeter;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lizhieffe on 3/12/18.
 */

public class AccelDataBank {
    private final static long BACKWARDS_LIMIT_MS = 10000;  // 10 seconds.
    private Deque<AccelData> mData;

    public AccelDataBank() {
        mData = new LinkedList<>();
    }

    synchronized public void addData(float x, float y) {
        long currTime = System.currentTimeMillis();
        filterInvalidateData();
        mData.addLast(new AccelData(x, y, currTime));
    }

    synchronized public List<AccelData> getData() {
        filterInvalidateData();
        List<AccelData> result = new ArrayList<>();
        for (Iterator<AccelData> itr = mData.iterator(); itr.hasNext();) {
            AccelData data = itr.next();
            result.add(new AccelData(data.ahead, data.right, data.timestamp_ms));
        }
        return result;
    }

    synchronized private void filterInvalidateData() {
        long currTime = System.currentTimeMillis();
        for (Iterator<AccelData> itr = mData.iterator(); itr.hasNext();) {
            AccelData data = itr.next();
            if (currTime - data.timestamp_ms >= BACKWARDS_LIMIT_MS) {
                itr.remove();
            }
        }
    }
}
