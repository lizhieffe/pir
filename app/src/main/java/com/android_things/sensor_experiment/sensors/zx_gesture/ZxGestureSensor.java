package com.android_things.sensor_experiment.sensors.zx_gesture;

/**
 * Created by lizhieffe on 1/1/18.
 */

public abstract class ZxGestureSensor implements AutoCloseable {
    /**
     * Gestures that the sensor supports.
     */
    public enum Gesture {
        SWIPE_RIGHT,
        SWIPE_LEFT,
        SWIPE_UP,
        HOVER,
        HOVER_LEFT,
        HOVER_RIGHT,
        HOVER_UP;

        // mapping from gesture codes to gestures
        private static final Gesture[] idMap = {
                null, SWIPE_RIGHT, SWIPE_LEFT, SWIPE_UP,
                null, HOVER, HOVER_LEFT, HOVER_RIGHT, HOVER_UP};

        static Gesture getGesture(int code) {
            if (code < 0 || code >= idMap.length) return null;
            return idMap[code];
        }
    }
}
