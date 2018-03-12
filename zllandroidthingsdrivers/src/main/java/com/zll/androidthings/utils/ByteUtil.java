package com.zll.androidthings.utils;

import android.util.Log;

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
    static public short byteToUnsignedShort(byte b) { return (short)(b & 0xff); }
    static public int byteToUnsignedInt(byte b) {
        return b & 0xff;
    }

    static public int twoBytesToSignedInt(byte high, byte low) {
        short fistPart = (short)((byteToUnsignedShort(high)<<8));
        short secondPart = (short)((byteToUnsignedShort(low))&0xffff);
        return (fistPart | secondPart);
    }

    static public int twoBytesToUnsignedInt(byte high, byte low) {
        short fistPart = (short)((byteToUnsignedShort(high)<<8)&0xffff);
        short secondPart = (short)((byteToUnsignedShort(low))&0xffff);
        return ((fistPart | secondPart) & 0xffff);
    }
}
