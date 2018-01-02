package com.android_things.sensor_experiment.utils;

/**
 * Created by lizhieffe on 1/1/18.
 */

public class ByteUtil {
    static public String byteToBinaryString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF))
                .replace(' ', '0');
    }

    // If convert directly, 0b1xxxxxxx will be interpreted as negative value.
    // This function makes sure it is interpreted as positive value.
    static public int byteToUnsignedInt(byte b) {
        return b & 0xff;
    }
}
