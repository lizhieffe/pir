package com.android_things.sensor_experiment.sensors;

import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * For Sparkfun SEN-14350 Ambient Light sensor.
 */

// TODO: change implementation interface.
@SuppressWarnings({"unused", "WeakerAccess"})
public class AmbientLightSen14350Sensor implements MotionSensor {
    public static final String DEFAULT_I2C_BUS = "I2C1";
    public static final int DEFAULT_I2C_ADDRESS = 0x39;

    private I2cDevice mDevice;
    public AmbientLightSen14350Sensor() {
        this(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS);
    }

    public AmbientLightSen14350Sensor(String bus, int address) {
        PeripheralManagerService pioService
                = new PeripheralManagerService();
        Log.d(TAG, "AmbientLightSen14350Sensor.AmbientLightSen14350Sensor: I2C bus lists: " + pioService.getI2cBusList());
        try {
            I2cDevice device = pioService.openI2cDevice(bus, address);
            connect(device);
        }  catch (IOException|RuntimeException e) {
            Log.d(TAG, "AmbientLightSen14350Sensor.AmbientLightSen14350Sensor: cannot connect: ", e);
        }
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {
        try {
            // Power down the device.
            mDevice.writeRegByte(CONTROL_REG, (byte) 0x0);

            mDevice.close();
        } catch (IOException e) {

        }
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

        try {
            // Power up device.
            mDevice.writeRegByte(CONTROL_REG, (byte) 0x3);

            byte power_byte = mDevice.readRegByte(CONTROL_REG);
            setGain(GainType.HIGH);

            setIntegrationTime(IntegrationTime.INT_TIME_402_MS);

            Thread.sleep(4000);
            for (int i = 0; i < 100; i++) {
                Log.d(TAG, "AmbientLightSen14350Sensor.connect: 444");
                byte level_read_lower = mDevice.readRegByte(DATA0LOW_REG);
                Log.d(TAG, "AmbientLightSen14350Sensor.connect: 555");
                byte level_read_upper = mDevice.readRegByte(DATA0HI_REG);
                Log.d(TAG, "AmbientLightSen14350Sensor.connect: 666");
                int level_read = (((int)level_read_upper) << 8) + (int)level_read_lower;
                Log.d(TAG, "AmbientLightSen14350Sensor.connect: level read = " + level_read);
                level_read_lower = mDevice.readRegByte(DATA1LOW_REG);
                level_read_upper = mDevice.readRegByte(DATA1HI_REG);
                level_read = (((int)level_read_upper) << 8) + (int)level_read_lower;
                Log.d(TAG, "AmbientLightSen14350Sensor.connect: level read = " + level_read);
                Thread.sleep(1000);
            }
        } catch (IOException|InterruptedException e) {
            Log.d(TAG, "AmbientLightSen14350Sensor.connect: cannot connect device: ", e);
        }
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

    public void setIntegrationTime(IntegrationTime time) {
        try {
            byte regVal = mDevice.readRegByte(TIMING_REG);
            regVal &= (byte)Integer.reverse(INTEGRATION_MASK);
            boolean shouldSet = true;
            switch (time) {
                case INT_TIME_13_7_MS:
                    regVal |= IntegrationTime.INT_TIME_13_7_MS.getFieldValue();
                    break;
                case INT_TIME_101_MS:
                    regVal |= IntegrationTime.INT_TIME_101_MS.getFieldValue();
                    break;
                case INT_TIME_402_MS:
                    regVal |= IntegrationTime.INT_TIME_402_MS.getFieldValue();
                    break;
                default:
                    shouldSet = false;
                    Log.e(TAG, "setIntegrationTime: incorrect time");
            }

            if (shouldSet) {
                mDevice.writeRegByte(TIMING_REG, regVal);
            }
        } catch (IOException e) {
            Log.e(TAG, "AmbientLightSen14350Sensor.setIntegrationTime: cannot set integration time: ", e);
        }

    }

    public IntegrationTime getIntegrationTime() {
        try {
            byte regVal = mDevice.readRegByte(TIMING_REG);
            Log.d(TAG, "AmbientLightSen14350Sensor.getIntegrationTime: regVal = " + regVal);
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
            Log.e(TAG, "AmbientLightSen14350Sensor.setIntegrationTime: cannot set integration time: ", e);
            return IntegrationTime.UNKNOWN;
        }
    }

    // High gain increases the detection sensitivity.
    public enum GainType {
        HIGH,
        LOW,
    }

    private void setGain(GainType gainType) {
        try {
            byte regVal = mDevice.readRegByte(TIMING_REG);
            regVal |= gainType == GainType.HIGH ? 0x00010000 : 0x00010000;
            mDevice.writeRegByte(TIMING_REG, regVal);
        } catch (IOException e) {
            Log.d(TAG, "AmbientLightSen14350Sensor.setHighGain: cannot set high gain: ", e);
        }
    }
}
