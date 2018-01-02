package com.android_things.sensor_experiment.sensors.zx_gesture;

import android.util.Log;

import com.android_things.sensor_experiment.utils.ByteUtil;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/1/18.
 */

public class ZxGestureSensor {
    public static final String DEFAULT_I2C_BUS = "I2C1";
    public static final int DEFAULT_I2C_ADDRESS = 0x10;
    public static final String DEFAULT_GPIO_PIN = "BCM27";

    // I2C bus and address for the device.
    private String mBus;
    private int mAddress;
    private String mGpioPin;

    private I2cDevice mDevice;
    private Gpio mGpio;

    public ZxGestureSensor() {
        this(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS, DEFAULT_GPIO_PIN);
    }

    public ZxGestureSensor(String bus, int address, String gpioPin) {
        mBus = bus;
        mAddress = address;
        mGpioPin = gpioPin;
    }

    public void startup() throws IOException {
        connect();
    }

    public void shutdown() {
        if (mDevice != null) {
            try {
                mDevice.close();
            } catch (IOException e) {
                Log.e(TAG, "ZxGestureSensor.shutdown: ", e);
            } finally {
                mDevice = null;
            }
        }

        if (mGpio != null) {
            try {
                mGpio.unregisterGpioCallback(onGpioDataReady);
                mGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "ZxGestureSensor.shutdown: ", e);
            } finally {
                mGpio = null;
            }
        }
    }

    public byte[] readPositions() {
        byte[] result = new byte[2];
        try {
            Log.d(TAG, "ZxGestureSensor.readPositions: status = " + mDevice.readRegByte(REG_STATUS));
            result[0] = mDevice.readRegByte(REG_XPOS);
            result[1] = mDevice.readRegByte(REG_ZPOS);
            Log.d(TAG, "ZxGestureSensor.connect: left emitter ranging data = "
                    + ByteUtil.byteToUnsignedInt(mDevice.readRegByte(LEFT_EMITTER_RANGING_DATA)));
            Log.d(TAG, "ZxGestureSensor.connect: right emitter ranging data = "
                    + ByteUtil.byteToUnsignedInt(mDevice.readRegByte(RIGHT_EMITTER_RANGING_DATA)));
        } catch (IOException e) {
            Log.e(TAG, "ZxGestureSensor.readPositions: ", e);
        }
        return result;
    }
    /**
     * I2C register addresses.
     * @see <a href="https://cdn.sparkfun.com/datasheets/Sensors/Proximity/XYZ%20I2C%20Registers%20v1.zip>XYZ I2C Register Map</a>
     */
    private static final int REG_STATUS = 0x00;
    private static final int REG_DRE = 0x01;
    private static final int REG_DRCFG = 0x02;
    private static final int REG_GESTURE = 0x04;
    private static final int REG_GESTURE_PARAM = 0x05;
    private static final int REG_XPOS = 0x08;
    private static final int REG_ZPOS = 0x0a;
    private static final int LEFT_EMITTER_RANGING_DATA = 0x0c;
    private static final int RIGHT_EMITTER_RANGING_DATA = 0x0d;
    private static final int REG_MAP_VERSION = 0xfe;
    private static final int SENSOR_MODEL = 0xff;

    private static final byte GESTURE_DRE_MASK = 0b00111111;
    private static final byte GESTURE_DRCFG_MASK = (byte)0b11000011;

    private void connect() throws IOException {
        PeripheralManagerService pioService
                = new PeripheralManagerService();
        Log.d(TAG, "ZxGestureSensor.startup: I2C bus list: " + pioService.getI2cBusList());

        Log.d(TAG, "ZxGestureSensor.connect: DRCFG MASK = " + (byte)GESTURE_DRCFG_MASK);


        mDevice = pioService.openI2cDevice(mBus, mAddress);
        mDevice.writeRegByte(REG_DRE, GESTURE_DRE_MASK);
        // mDevice.writeRegByte(REG_DRCFG, GESTURE_DRCFG_MASK);

        byte regMapVersion = mDevice.readRegByte(REG_MAP_VERSION);
        assert(regMapVersion == 1);

        Log.d(TAG, "ZxGestureSensor.connect: reg map version = " + mDevice.readRegByte(REG_MAP_VERSION));
        Log.d(TAG, "ZxGestureSensor.connect: sensor model = " + mDevice.readRegByte(SENSOR_MODEL));

        mGpio = pioService.openGpio(mGpioPin);
        mGpio.setDirection(Gpio.DIRECTION_IN);
        mGpio.setActiveType(Gpio.ACTIVE_HIGH);
        mGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
        mGpio.registerGpioCallback(onGpioDataReady);
    }

    private GpioCallback onGpioDataReady = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.d(TAG, "ZxGestureSensor.onGpioEdge: data ready: " + gpio.getValue());
            } catch (IOException e) {
                Log.e(TAG, "ZxGestureSensor.onGpioEdge: ", e);
            }
            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.e(TAG, "ZxGestureSensor.onGpioError: gpio error: " + error);
        }
    };
}