package com.android_things.sensor_experiment.base;

/**
 * Created by lizhieffe on 1/1/18.
 */

public class Features {
    public final static boolean AIR_QUALITRY_DETECTION_ENABLED = false;
    public final static boolean ACCELEROMETER_ENABLED = true;
    public final static boolean AUDIO_RECORD_ENABLED = false;
    public final static boolean AMBIENT_LIGHT_DETECTION_ENABLED = false;
    public final static boolean GESTURE_DETECTION_ENABLED = false;

    public final static boolean BME_280_SENSOR_ENABLED = false;

    public final static boolean PMS_7003_SENSOR_ENABLED = false;

    public final static boolean TCS_34725_SENSOR_ENABLED = false;

    public final static boolean MOTION_DETECTION_ENABLED = false;
    /*
     * These two fields are effective only when MOTION_DETECTION_ENABLED is true.
     */
    // Whether to enabled PIR based motion detection.
    public final static boolean MOTION_DETECTION_PIR_ENABLED = false;
    // Whether to enable proximity based motion detection.
    public final static boolean MOTION_DETECTION_PROXIMITY_ENABLED = false;
}
