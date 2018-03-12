package com.zll.androidthings.drivers.pms_7003;

import android.support.annotation.Nullable;

import com.zll.androidthings.utils.ByteUtil;

/**
 * Created by lizhieffe on 1/24/18.
 */

class Pms7003SensorDataParser {
    static final private byte START_BYTE_1 = (byte)0x42;
    static final private byte START_BYTE_2 = (byte)0x4d;

    private int parseStage = 0;

    private byte mHigh;
    private byte mLow;

    private byte[] mCheck = new byte[2];

    private Pms7003SensorData mData = new Pms7003SensorData();

    // Return null when the parse result is not available - keep feed in more
    // data read from sensor.
    @Nullable
    Pms7003SensorData parse(byte b) {
        // Log.d(TAG, "Pms7003SensorDataParser.parse: stage = " +parseStage);
        switch (parseStage) {
            case 0:
                if (b == START_BYTE_1) {
                    parseStage++;
                } else {
                    startOver();
                }
                return null;
            case 1:
                if (b == START_BYTE_2) {
                    parseStage++;
                } else {
                    startOver();
                }
                return null;
            case 30:
                mCheck[0] = b;
                parseStage++;
                return null;
            case 31:
                mCheck[1] = b;
                Pms7003SensorData result = null;
                if (checkSum()) {
                    result = new Pms7003SensorData(mData);
                }
                startOver();
                return result;
            default:
                if (parseStage % 2 == 0) {
                    mHigh = b;
                } else {
                    mLow = b;
                    if (parseStage == 11) {
                        mData.mPm1_0 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 13) {
                        mData.mPm2_5 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 15) {
                        mData.mPm10 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 17) {
                        mData.mNumParticles0_3 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 19) {
                        mData.mNumParticles0_5 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 21) {
                        mData.mNumParticles1_0 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 23) {
                        mData.mNumParticles2_5 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 25) {
                        mData.mNumParticles5_0 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    } else if (parseStage == 27) {
                        mData.mNumParticles10 = ByteUtil.twoBytesToUnsignedInt(mHigh, mLow);
                    }
                }
                parseStage++;
                return null;

        }
    }

    private void startOver() {
        mData = new Pms7003SensorData();
        parseStage = 0;
        mHigh = 0;
        mLow = 0;
        mCheck[0] = 0;
        mCheck[1] = 0;
    }

    private boolean checkSum() {
        // TODO: add checksum
        return true;
    }
}
