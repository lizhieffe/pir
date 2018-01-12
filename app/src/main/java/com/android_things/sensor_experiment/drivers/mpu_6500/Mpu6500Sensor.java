package com.android_things.sensor_experiment.drivers.mpu_6500;

import android.util.Log;

import com.android_things.sensor_experiment.drivers.MotionSensor;
import com.android_things.sensor_experiment.utils.ByteUtil;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * For MPU-6500 6-axis MotionTracking device with I2C interface.
 *
 * A well written c driver is: https://github.com/NordicPlayground/nrf52-quadcopter/blob/master/Firmware/drivers/mpu6500.h
 */

public class Mpu6500Sensor implements MotionSensor {
    public static final String DEFAULT_I2C_BUS = "I2C1";
    // 0x68 when AD0 pin is low (GND), 0x69 when AD0 is high (3.3V)
    public static final int DEFAULT_I2C_ADDRESS = 0x68;

    // I2C bus and address for the device.
    private String mBus;
    private int mAddress;

    private I2cDevice mDevice;

    public Mpu6500Sensor() {
        this(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS);
    }

    public Mpu6500Sensor(String bus, int address) {
        mBus = bus;
        mAddress = address;
    }

    @Override
    public void startup() throws IOException {
        PeripheralManagerService pioService
                = new PeripheralManagerService();
        Log.d(TAG, "Mpu6500Sensor.startup: I2C bus lists: " + pioService.getI2cBusList());
        I2cDevice device = pioService.openI2cDevice(mBus, mAddress);
        connect(device);
    }

    @Override
    public void shutdown() {
        try {
            // powerOff();
            mDevice.close();
        } catch (IOException e) {

        }
    }

    private static final int FIFO_ENABLE_REG = 35;
    private static final int INT_CONFIG_REG = 55;
    private static final int INT_ENABLE_REG = 56;
    private static final int INT_STATUS_REG = 58;
    private static final int ACCEL_XOUT_H = 59;
    private static final int ACCEL_XOUT_L = 60;
    private static final int ACCEL_YOUT_H = 61;
    private static final int ACCEL_YOUT_L = 62;
    private static final int ACCEL_ZOUT_H = 63;
    private static final int ACCEL_ZOUT_L = 64;
    private static final int SIGNAL_PATH_RESET = 104;
    private static final int ACCEL_INTEL_CTRL = 105;
    private static final int PWR_MGMT_1 = 107;
    private static final int PWR_MGMT_2 = 108;
    private static final int WHOAMI_REG = 117;

    // Return value is an int array with size 3. If read fails, return null.
    public int[] readAccelData() {
        try {
            int x = ByteUtil.twoBytesToUnsignedShort(
                        mDevice.readRegByte(ACCEL_XOUT_H),
                        mDevice.readRegByte(ACCEL_XOUT_L));
            int y = ByteUtil.twoBytesToUnsignedShort(
                    mDevice.readRegByte(ACCEL_YOUT_H),
                    mDevice.readRegByte(ACCEL_YOUT_L));
            int z = ByteUtil.twoBytesToUnsignedShort(
                    mDevice.readRegByte(ACCEL_ZOUT_H),
                    mDevice.readRegByte(ACCEL_ZOUT_L));
            return new int[]{x, y, z};
        } catch (IOException e) {
            Log.e(TAG, "Mpu6500Sensor.readAccelData: ", e);
            return null;
        }
    }

    private void connect(I2cDevice device) {
        mDevice = device;

        try {
            reset();
            selfTest();
            config();
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Mpu6500Sensor.connect: ", e);
        }
    }

    // Best practice is to reset device every time after the app is restarted.
    private void reset() throws IOException, InterruptedException {
        mDevice.writeRegByte(PWR_MGMT_1, (byte)0b10000000);
        Thread.sleep(150);
        mDevice.writeRegByte(SIGNAL_PATH_RESET, (byte)0b00000111);
        Thread.sleep(150);
    }

    // TODO: read register map doc and add more self test.
    private void selfTest() throws IOException {
        byte whoamiVal = mDevice.readRegByte(WHOAMI_REG);
        assert(ByteUtil.byteToUnsignedInt(whoamiVal) == 0x70);

        Log.d(TAG, "Mpu6500Sensor.selfTest: sensor self test passed.");
    }

    private void config() throws IOException {
        // Enable temp/gyro/accel output in FIFO.
        mDevice.writeRegByte(FIFO_ENABLE_REG, (byte)0b11111000);

        mDevice.writeRegByte(INT_CONFIG_REG, (byte)0b00000000);
        // Enable interrupt only for wake on motion.
        mDevice.writeRegByte(INT_ENABLE_REG, (byte)0b01000000);
        // Enable Wake-on-Motion detection logic.
        mDevice.writeRegByte(ACCEL_INTEL_CTRL, (byte)0b10000000);

        // Disable temperature sensor.
        mDevice.writeRegByte(PWR_MGMT_1, (byte)0b00001000);
        // Enable accelerometer and gyro.
        mDevice.writeRegByte(PWR_MGMT_2, (byte)0b00000000);
    }
}
