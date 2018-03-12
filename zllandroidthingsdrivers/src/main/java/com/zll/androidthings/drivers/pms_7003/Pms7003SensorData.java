package com.zll.androidthings.drivers.pms_7003;

/**
 * Created by lizhieffe on 1/24/18.
 */

public class Pms7003SensorData {
    // Concentration unit ug/m3 under atmospheric env.
    public int mPm1_0;
    public int mPm2_5;
    public int mPm10;

    // Number of particles with diameters beyond corresponding number (um)
    // in 0.1 L of air.
    public int mNumParticles0_3;
    public int mNumParticles0_5;
    public int mNumParticles1_0;
    public int mNumParticles2_5;
    public int mNumParticles5_0;
    public int mNumParticles10;

    Pms7003SensorData() {
        clear();
    }

    public Pms7003SensorData(int[] data) {
        assert (data.length == 3);
        mPm1_0 = data[0];
        mPm2_5 = data[1];
        mPm10 = data[2];
    }

    public Pms7003SensorData(Pms7003SensorData data) {
        mPm1_0 = data.mPm1_0;
        mPm2_5 = data.mPm2_5;
        mPm10 = data.mPm10;
        mNumParticles0_3 = data.mNumParticles0_3;
        mNumParticles0_5 = data.mNumParticles0_5;
        mNumParticles1_0 = data.mNumParticles1_0;
        mNumParticles2_5 = data.mNumParticles2_5;
        mNumParticles5_0 = data.mNumParticles5_0;
        mNumParticles10 = data.mNumParticles10;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[PM1.0]");
        sb.append(mPm1_0);
        sb.append(" ");
        sb.append("[PM2.5]");
        sb.append(mPm2_5);
        sb.append(" ");
        sb.append("[PM10]");
        sb.append(mPm10);
        // TODO: print other data as well.
        return sb.toString();
    }

    public float[] toFloatArray() {
        float[] data = new float[3];
        data[0] = mPm1_0;
        data[1] = mPm2_5;
        data[2] = mPm10;
        return data;
    }

    void clear() {
        mPm1_0 = 0;
        mPm2_5 = 0;
        mPm10 = 0;
        mNumParticles0_3 = 0;
        mNumParticles0_5 = 0;
        mNumParticles1_0 = 0;
        mNumParticles2_5 = 0;
        mNumParticles5_0 = 0;
        mNumParticles10 = 0;
    }
}
