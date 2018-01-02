package com.android_things.sensor_experiment.sensors;

import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import junit.framework.Assert;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * For APDS 9301 Ambient Light sensor with I2C interface.
 */

// TODO: change implementation interface.
@SuppressWarnings({"unused", "WeakerAccess"})
public class Apds9301Sensor implements MotionSensor {
    public static final String DEFAULT_I2C_BUS = "I2C1";
    public static final int DEFAULT_I2C_ADDRESS = 0x39;

    // I2C bus and address for the device.
    private String mBus;
    private int mAddress;

    private I2cDevice mDevice;

    public Apds9301Sensor() {
        this(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS);
    }

    public Apds9301Sensor(String bus, int address) {
        mBus = bus;
        mAddress = address;
    }

    @Override
    public void startup() throws IOException {
        PeripheralManagerService pioService
                = new PeripheralManagerService();
        Log.d(TAG, "Apds9301Sensor.Apds9301Sensor: I2C bus lists: " + pioService.getI2cBusList());
        I2cDevice device = pioService.openI2cDevice(mBus, mAddress);
        connect(device);
    }

    @Override
    public void shutdown() {
        try {
            powerOff();
            mDevice.close();
        } catch (IOException e) {

        }
    }

    public float readLuxLevel() {
        int ch0Int = getCH0Level();  // to mimic unsigned int
        int ch1Int = getCH1Level();  // to mimic unsigned int
        float ch0 = (float)getCH0Level();
        float ch1 = (float)getCH1Level();
        Assert.assertEquals(getIntegrationTime(), IntegrationTime.INT_TIME_402_MS);
        switch (getIntegrationTime()) {
            case INT_TIME_13_7_MS:
                if ((ch1Int >= 5047) || (ch0Int >= 5047))
                {
                    return (float)(1.0/0.0);
                }
                break;
            case INT_TIME_101_MS:
                if ((ch1Int >= 37177) || (ch0Int >= 37177))
                {
                    return (float)(1.0/0.0);
                }
                break;
            case INT_TIME_402_MS:
                if ((ch1Int >= 65535) || (ch0Int >= 65535))
                {
                    return (float)(1.0/0.0);
                }
                break;
        }
        float ratio = ch1/ch0;
        Assert.assertEquals(getIntegrationTime(), IntegrationTime.INT_TIME_402_MS);
        switch (getIntegrationTime())
        {
            case INT_TIME_13_7_MS:
                ch0 *= 1/0.034;
                ch1 *= 1/0.034;
                break;
            case INT_TIME_101_MS:
                ch0 *= 1/0.252;
                ch1 *= 1/0.252;
                break;
            case INT_TIME_402_MS:
                ch0 *= 1;
                ch1 *= 1;
                break;
        }

        Assert.assertEquals(getGain(), GainType.HIGH);
        if (getGain() == GainType.HIGH)
        {
            ch0 /= 16;
            ch1 /= 16;
        }

        float luxVal = (float)0.0;
        if (ratio <= 0.5)
        {
            luxVal = (float)((0.0304 * ch0) - ((0.062 * ch0) * (Math.pow((ch1/ch0), 1.4))));
        }
        else if (ratio <= 0.61)
        {
            luxVal = (float)((0.0224 * ch0) - (0.031 * ch1));
        }
        else if (ratio <= 0.8)
        {
            luxVal = (float)((0.0128 * ch0) - (0.0153 * ch1));
        }
        else if (ratio <= 1.3)
        {
            luxVal = (float)((0.00146 * ch0) - (0.00112*ch1));
        }

        return luxVal;
    }


    // Values are obtained from official driver for Arduino.
    // https://github.com/sparkfun/SparkFun_APDS9301_Library/blob/master/src/Sparkfun_APDS9301_Library.h
    private static final int CONTROL_REG = 0x80;
    private static final int TIMING_REG = 0x81;
    private static final int THRESHLOWLOW_REG = 0x82;
    private static final int THRESHLOWHI_REG = 0x83;
    private static final int THRESHHILOW_REG = 0x84;
    private static final int THRESHHIHI_REG = 0x85;
    private static final int INTERRUPT_REG = 0x86;
    private static final int ID_REG = 0x8A;
    private static final int DATA0LOW_REG = 0x8C;
    private static final int DATA0HI_REG = 0x8D;
    private static final int DATA1LOW_REG = 0x8E;
    private static final int DATA1HI_REG = 0x8F;

    private void connect(I2cDevice device) {
        mDevice = device;

        // Power up device.
        powerOn();
        setGain(GainType.HIGH);
        setIntegrationTime(IntegrationTime.INT_TIME_402_MS);
    }

    // Sensitivity to light increases with integration time, and the rate at which new data is
    // generated by the sensor is also determined by integration time. By default, the integration
    // time is set to the lowest value (13.7ms).
    public enum IntegrationTime {
        UNKNOWN((byte)0b11),
        INT_TIME_13_7_MS((byte)0b00),
        INT_TIME_101_MS((byte)0b01),
        INT_TIME_402_MS((byte)0b10);

        byte mFieldValue;

        IntegrationTime(byte fieldValue) {
            mFieldValue = fieldValue;
        }

        byte getFieldValue() {
            return mFieldValue;
        }
    }

    private static final byte INTEGRATION_MASK = 0b00000011;

    private float mResolution;
    private float mMaxRange;

    public void setIntegrationTime(IntegrationTime time) {
        try {
            byte regVal = mDevice.readRegByte(TIMING_REG);
            regVal &= ~INTEGRATION_MASK;
            boolean shouldSet = true;
            switch (time) {
                case INT_TIME_13_7_MS:
                    regVal |= IntegrationTime.INT_TIME_13_7_MS.getFieldValue();
                    mResolution = SENSITIVITY_RANGE_13_7_MS[0];
                    mMaxRange = SENSITIVITY_RANGE_13_7_MS[1];
                    break;
                case INT_TIME_101_MS:
                    regVal |= IntegrationTime.INT_TIME_101_MS.getFieldValue();
                    mResolution = SENSITIVITY_RANGE_101_MS[0];
                    mMaxRange = SENSITIVITY_RANGE_101_MS[1];
                    break;
                case INT_TIME_402_MS:
                    regVal |= IntegrationTime.INT_TIME_402_MS.getFieldValue();
                    mResolution = SENSITIVITY_RANGE_402_MS[0];
                    mMaxRange = SENSITIVITY_RANGE_402_MS[1];
                    break;
                default:
                    shouldSet = false;
                    Log.e(TAG, "setIntegrationTime: incorrect time");
            }

            if (shouldSet) {
                mDevice.writeRegByte(TIMING_REG, regVal);
            }
        } catch (IOException e) {
            Log.e(TAG, "Apds9301Sensor.setIntegrationTime: cannot set integration time: ", e);
        }

    }

    public IntegrationTime getIntegrationTime() {
        try {
            byte regVal = mDevice.readRegByte(TIMING_REG);
            byte integrationVal = (byte)(regVal & INTEGRATION_MASK);
            switch (integrationVal) {
                case 0b00000000:
                    return IntegrationTime.INT_TIME_13_7_MS;
                case 0b00000001:
                    return IntegrationTime.INT_TIME_101_MS;
                case 0b00000010:
                    return IntegrationTime.INT_TIME_402_MS;
                default:
                    return IntegrationTime.UNKNOWN;
            }
        } catch (IOException e) {
            Log.e(TAG, "Apds9301Sensor.setIntegrationTime: cannot set integration time: ", e);
            return IntegrationTime.UNKNOWN;
        }
    }

    // TODO: these values are borrowed from other senser's values. Figure out the correct values.
    //
    // Ambient light resolution is affected by the integration time.
    public static final float[] SENSITIVITY_RANGE_13_7_MS = { 0.088f, 5741 };
    public static final float[] SENSITIVITY_RANGE_101_MS = { 0.012f,  786 };
    public static final float[] SENSITIVITY_RANGE_402_MS = { 0.003f,  197 };

    public float getCurrentResolution() {
        return mResolution;
    }

    public float getCurrentMaxRange() {
        return mMaxRange;
    }

    // High gain increases the detection sensitivity.
    public enum GainType {
        UNKNOWN,
        HIGH,
        LOW,
    }

    private static final byte GAIN_MASK = 0b00010000;

    private GainType getGain() {
        try {
            byte regVal = mDevice.readRegByte(TIMING_REG);
            return (regVal & GAIN_MASK) == 0 ? GainType.LOW : GainType.HIGH;
        } catch (IOException e) {
            Log.d(TAG, "Apds9301Sensor.setHighGain: cannot set high gain: ", e);
            return GainType.UNKNOWN;
        }
    }

    private void setGain(GainType gainType) {
        try {
            byte regVal = mDevice.readRegByte(TIMING_REG);
            if (gainType == GainType.HIGH) {
                regVal |= 0b00010000;
            } else {
                regVal &= ~0b00010000;
            }
            mDevice.writeRegByte(TIMING_REG, regVal);
            regVal = mDevice.readRegByte(TIMING_REG);
        } catch (IOException e) {
            Log.e(TAG, "Apds9301Sensor.setHighGain: cannot set high gain: ", e);
        }
    }

    private int getCH0Level() {
        try {
            // int val = mDevice.readRegWord(DATA0LOW_REG);
            // return val & 0x0000FFFF;
            byte lowerVal = mDevice.readRegByte(DATA0LOW_REG);
            byte upperVal = mDevice.readRegByte(DATA0HI_REG);
            return ((((int) upperVal) << 8) + (int) lowerVal) & 0x0000FFFF;
        } catch (IOException e) {
            Log.e(TAG, "getCH0Level: cannot get CH0 level: ", e);
            return 0;
        }
    }

    private int getCH1Level() {
        try {
            byte lowerVal = mDevice.readRegByte(DATA1LOW_REG);
            byte upperVal = mDevice.readRegByte(DATA1HI_REG);
            return ((((int) upperVal) << 8) + (int) lowerVal) & 0x0000FFFF;
        } catch (IOException e) {
            Log.e(TAG, "getCH0Level: cannot get CH1 level: ", e);
            return 0;
        }
    }

    private void powerOn() {
        try {
            byte regVal = mDevice.readRegByte(CONTROL_REG);
            mDevice.writeRegByte(CONTROL_REG, (byte)(regVal | (byte) 0x3));

            regVal = mDevice.readRegByte(CONTROL_REG);
            Assert.assertEquals(regVal & 0x3, (byte)0x3);
        } catch (IOException e) {
            Log.e(TAG, "Apds9301Sensor.powerOn: ", e);
        }
    }

    private void powerOff() {
        try {
            byte regVal = mDevice.readRegByte(CONTROL_REG);
            mDevice.writeRegByte(CONTROL_REG, (byte)(regVal | (byte) 0x0));
        } catch (IOException e) {
            Log.e(TAG, "Apds9301Sensor.powerOn: ", e);
        }
    }

    // Return unit is us (1e-6 sec)
    public int getIntegrationTimeValueInUs() {
        IntegrationTime integrationTime = getIntegrationTime();
        switch (integrationTime) {
            case INT_TIME_13_7_MS:
                return 13700;
            case INT_TIME_101_MS:
                return 101000;
            case INT_TIME_402_MS:
                return 402000;
            default:
                return -1;
        }
    }
}
