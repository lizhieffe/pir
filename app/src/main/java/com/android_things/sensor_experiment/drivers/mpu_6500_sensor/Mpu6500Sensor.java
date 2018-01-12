package com.android_things.sensor_experiment.drivers.mpu_6500_sensor;

import android.util.Log;

import com.android_things.sensor_experiment.drivers.MotionSensor;
import com.android_things.sensor_experiment.utils.ByteUtil;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * For InvenSense MPU-6500 6-axis MotionTracking device with I2C interface.
 *
 * A well written c driver is: https://github.com/NordicPlayground/nrf52-quadcopter/blob/master/Firmware/drivers/mpu6500.h
 */

public class Mpu6500Sensor implements MotionSensor {
    public static final String DEFAULT_I2C_BUS = "I2C1";

    // 0x68 when AD0 pin is low (GND), 0x69 when AD0 is high (3.3V)
    private static final int I2C_ADDRESS_LOW = 0x68;
    private static final int I2C_ADDRESS_HIGH = 0x69;
    public static final int DEFAULT_I2C_ADDRESS = I2C_ADDRESS_LOW;

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

    private static final int SELF_TEST_X_GYRO = 0;
    private static final int SELF_TEST_Y_GYRO = 1;
    private static final int SELF_TEST_Z_GYRO = 2;
    private static final int SELF_TEST_X_ACCEL = 13;
    private static final int SELF_TEST_Y_ACCEL = 14;
    private static final int SELF_TEST_Z_ACCEL = 15;
    private static final int GYRO_CONFIG_REG = 27;
    private static final int ACCEL_CONFIG_REG = 28;
    private static final int ACCEL_CONFIG_REG_2 = 29;
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
    private static final int GYRO_XOUT_H = 67;
    private static final int GYRO_XOUT_L = 68;
    private static final int GYRO_YOUT_H = 69;
    private static final int GYRO_YOUT_L = 70;
    private static final int GYRO_ZOUT_H = 71;
    private static final int GYRO_ZOUT_L = 72;
    private static final int SIGNAL_PATH_RESET = 104;
    private static final int ACCEL_INTEL_CTRL = 105;
    private static final int PWR_MGMT_1 = 107;
    private static final int PWR_MGMT_2 = 108;
    private static final int WHOAMI_REG = 117;

    // Response is in unit of acceleration g.
    public float[] readAccelData() {
        int[] rawData = readAccelRawData();
        float[] data = new float[3];
        for (int i = 0; i < data.length; i++) {
            data[i] = (float)rawData[i] / (float)(65536.0 / 2.0 / 2.0);
        }
        return data;
    }

    // TODO: use burst read as mentioned in L1953 to make sure the read numbers are for the same sampling instance.
    //
    // TODO: change the raw data mapping based on measuring setting.
    //
    // Return value is an int array with size 3. If read fails, return null.
    // The raw data read value is within range [-2^15, 2^15], that is [-32768, 32767].
    // The default measuring range setting for accelerometer is +/-2g. That means +/-2g is mapped
    // to +/-2^15 linearly. If the measuring range is changed, the mapping also changes.
    //
    // Here has some explanation for the data: https://www.i2cdevlib.com/forums/topic/4-understanding-raw-values-of-accelerometer-and-gyrometer/
    public int[] readAccelRawData() {
        try {
            int x = ByteUtil.twoBytesToSignedInt(
                        mDevice.readRegByte(ACCEL_XOUT_H),
                        mDevice.readRegByte(ACCEL_XOUT_L));
            int y = ByteUtil.twoBytesToSignedInt(
                    mDevice.readRegByte(ACCEL_YOUT_H),
                    mDevice.readRegByte(ACCEL_YOUT_L));
            int z = ByteUtil.twoBytesToSignedInt(
                    mDevice.readRegByte(ACCEL_ZOUT_H),
                    mDevice.readRegByte(ACCEL_ZOUT_L));
            return new int[]{x, y, z};
        } catch (IOException e) {
            Log.e(TAG, "Mpu6500Sensor.readAccelData: ", e);
            return null;
        }
    }

    public int[] readGyroRawData() {
        try {
            int x = ByteUtil.twoBytesToSignedInt(
                    mDevice.readRegByte(GYRO_XOUT_H),
                    mDevice.readRegByte(GYRO_XOUT_L));
            int y = ByteUtil.twoBytesToSignedInt(
                    mDevice.readRegByte(GYRO_YOUT_H),
                    mDevice.readRegByte(GYRO_YOUT_L));
            int z = ByteUtil.twoBytesToSignedInt(
                    mDevice.readRegByte(GYRO_ZOUT_H),
                    mDevice.readRegByte(GYRO_ZOUT_L));
            return new int[]{x, y, z};
        } catch (IOException e) {
            Log.e(TAG, "Mpu6500Sensor.readGyroData: ", e);
            return null;
        }
    }

    public int[] readExpectedAccelSelfTestData() {
        try {
            int x = ByteUtil.byteToUnsignedInt(mDevice.readRegByte(SELF_TEST_X_ACCEL));
            int y = ByteUtil.byteToUnsignedInt(mDevice.readRegByte(SELF_TEST_Y_ACCEL));
            int z = ByteUtil.byteToUnsignedInt(mDevice.readRegByte(SELF_TEST_Z_ACCEL));
            return new int[]{x, y, z};
        } catch (IOException e) {
            Log.e(TAG, "Mpu6500Sensor.readExpectedAccelSelfTestData: ", e);
            return null;
        }
    }

    public int[] readExpectedGyroSelfTestData() {
        try {
            int x = ByteUtil.byteToUnsignedInt(mDevice.readRegByte(SELF_TEST_X_GYRO));
            int y = ByteUtil.byteToUnsignedInt(mDevice.readRegByte(SELF_TEST_Y_GYRO));
            int z = ByteUtil.byteToUnsignedInt(mDevice.readRegByte(SELF_TEST_Z_GYRO));
            return new int[]{x, y, z};
        } catch (IOException e) {
            Log.e(TAG, "Mpu6500Sensor.readExpectedGyroSelfTestData: ", e);
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

    static final int mpu6500StTb[] = {
            2620,2646,2672,2699,2726,2753,2781,2808, //7
            2837,2865,2894,2923,2952,2981,3011,3041, //15
            3072,3102,3133,3165,3196,3228,3261,3293, //23
            3326,3359,3393,3427,3461,3496,3531,3566, //31
            3602,3638,3674,3711,3748,3786,3823,3862, //39
            3900,3939,3979,4019,4059,4099,4140,4182, //47
            4224,4266,4308,4352,4395,4439,4483,4528, //55
            4574,4619,4665,4712,4759,4807,4855,4903, //63
            4953,5002,5052,5103,5154,5205,5257,5310, //71
            5363,5417,5471,5525,5581,5636,5693,5750, //79
            5807,5865,5924,5983,6043,6104,6165,6226, //87
            6289,6351,6415,6479,6544,6609,6675,6742, //95
            6810,6878,6946,7016,7086,7157,7229,7301, //103
            7374,7448,7522,7597,7673,7750,7828,7906, //111
            7985,8065,8145,8227,8309,8392,8476,8561, //119
            8647,8733,8820,8909,8998,9088,9178,9270,
            9363,9457,9551,9647,9743,9841,9939,10038,
            10139,10240,10343,10446,10550,10656,10763,10870,
            10979,11089,11200,11312,11425,11539,11654,11771,
            11889,12008,12128,12249,12371,12495,12620,12746,
            12874,13002,13132,13264,13396,13530,13666,13802,
            13940,14080,14221,14363,14506,14652,14798,14946,
            15096,15247,15399,15553,15709,15866,16024,16184,
            16346,16510,16675,16842,17010,17180,17352,17526,
            17701,17878,18057,18237,18420,18604,18790,18978,
            19167,19359,19553,19748,19946,20145,20347,20550,
            20756,20963,21173,21385,21598,21814,22033,22253,
            22475,22700,22927,23156,23388,23622,23858,24097,
            24338,24581,24827,25075,25326,25579,25835,26093,
            26354,26618,26884,27153,27424,27699,27976,28255,
            28538,28823,29112,29403,29697,29994,30294,30597,
            30903,31212,31524,31839,32157,32479,32804,33132,
    };

    private final static int SELF_TEST_READ_COUNT = 200;

    /** Evaluate the values from a MPU6500 self test.
     * @param low The low limit of the self test
     * @param high The high limit of the self test
     * @param value The value to compare with.
     * @param string A pointer to a string describing the value.
     * @return True if self test within low - high limit, false otherwise
     */
    boolean evaluateSelfTest(float low, float high, float value, String str)
    {
        if (value < low || value > high) {
            Log.e(TAG, "evaluateSelfTest: self test " + str
            + "[FAIL]. low: " + low + ", high: " + high + ", measured: " + value);
            return false;
        } else {
            Log.d(TAG, "evaluateSelfTest: self test " + str
                    + "[SUCCEED]. low: " + low + ", high: " + high + ", measured: " + value);
            return true;
        }
    }

    private final static float SELF_TEST_ACCEL_LOW = -14;
    private final static float SELF_TEST_ACCEL_HIGH = 14;
    private final static float SELF_TEST_GYRO_LOW = -14;
    private final static float SELF_TEST_GYRO_HIGH = 14;

    // TODO: read register map doc and add more self test.
    private void selfTest() throws IOException, InterruptedException {
        byte whoamiVal = mDevice.readRegByte(WHOAMI_REG);
        assert(ByteUtil.byteToUnsignedInt(whoamiVal) == 0x70);

        // Get average current value of accelrometer.
        int[] accelAvg = readAvgAccelData(SELF_TEST_READ_COUNT);
        int[] gyroAvg = readAvgGyroData(SELF_TEST_READ_COUNT);

        // Config the accelerometer for self-test.
        mDevice.writeRegByte(ACCEL_CONFIG_REG, (byte)0xE0);  // Enable self test on all three axes and set accelerometer range to +/- 2 g
        mDevice.writeRegByte(GYRO_CONFIG_REG, (byte)0xE0);  // Enable self test on all three axes and set gyro range to +/- 250 degrees/s
        Thread.sleep(25);  // Delay to let the device stabilize.
        int[] accelSelfTestAvg = readAvgAccelData(SELF_TEST_READ_COUNT);
        int[] gyroSelfTestAvg = readAvgGyroData(SELF_TEST_READ_COUNT);

        // Config the accelerometer for normal operation.
        mDevice.writeRegByte(ACCEL_CONFIG_REG, (byte)0x00);
        mDevice.writeRegByte(GYRO_CONFIG_REG, (byte)0x00);
        Thread.sleep(25);  // Delay to let the device stabilize.

        int[] expectedAccelSelfTest = readExpectedAccelSelfTestData();
        int[] expectedGyroSelfTest = readExpectedGyroSelfTestData();

        int[] factoryTrim = new int[6];
        for (int i = 0; i < 6; i++) {
            // TODO: add gyro test and remove the i<3 check.
            if (i < 3) {
                if (expectedAccelSelfTest[i] != 0) {
                    factoryTrim[i] = mpu6500StTb[expectedAccelSelfTest[i] - 1];
                } else {
                    factoryTrim[i] = 0;
                }
            } else {
                if (expectedGyroSelfTest[i - 3] != 0) {
                    factoryTrim[i] = mpu6500StTb[expectedGyroSelfTest[i - 3] - 1];
                } else {
                    factoryTrim[i] = 0;
                }
            }
        }

        float[] accelDiff = new float[3];
        float[] gyroDiff = new float[3];
        for (int i = 0; i < 3; i++) {
            accelDiff[i] = 100.0f * ((float)((accelSelfTestAvg[i] - accelAvg[i]) - factoryTrim[i]))/factoryTrim[i];
            gyroDiff[i] = 100.0f * ((float)((gyroSelfTestAvg[i] - gyroAvg[i]) - factoryTrim[i + 3]))/factoryTrim[i + 3];
        }

        if (evaluateSelfTest(SELF_TEST_ACCEL_LOW, SELF_TEST_ACCEL_HIGH,
                    accelDiff[0], "accel X") &&
            evaluateSelfTest(SELF_TEST_ACCEL_LOW, SELF_TEST_ACCEL_HIGH,
                    accelDiff[1], "accel Y") &&
            evaluateSelfTest(SELF_TEST_ACCEL_LOW, SELF_TEST_ACCEL_HIGH,
                    accelDiff[2], "accel Z") &&
            evaluateSelfTest(SELF_TEST_GYRO_LOW, SELF_TEST_GYRO_HIGH,
                    gyroDiff[0], "gyro X") &&
            evaluateSelfTest(SELF_TEST_GYRO_LOW, SELF_TEST_GYRO_HIGH,
                    gyroDiff[1], "gyro Y") &&
            evaluateSelfTest(SELF_TEST_GYRO_LOW, SELF_TEST_GYRO_HIGH,
                    gyroDiff[2], "gyro Z")) {
            Log.d(TAG, "Mpu6500Sensor.selfTest: sensor self test passed.");
        } else {
            Log.e(TAG, "selfTest: sensor self test failed!!!");
        }
    }

    private int[] readAvgAccelData(int count) {
        int[] avg = new int[3];
        for (int i = 0; i < count; i++) {
            int[] data = readAccelRawData();
            for (int j = 0; j < avg.length; j++) {
                avg[j] += data[j];
            }
        }
        for (int j = 0; j < avg.length; j++) {
            avg[j] /= count;
        }
        return avg;
    }

    private int[] readAvgGyroData(int count) {
        int[] avg = new int[3];
        for (int i = 0; i < count; i++) {
            int[] data = readGyroRawData();
            for (int j = 0; j < avg.length; j++) {
                avg[j] += data[j];
            }
        }
        for (int j = 0; j < avg.length; j++) {
            avg[j] /= count;
        }
        return avg;
    }

    private void config() throws IOException {
        // // Enable temp/gyro/accel output in FIFO.
        // mDevice.writeRegByte(FIFO_ENABLE_REG, (byte)0b11111000);

        // mDevice.writeRegByte(INT_CONFIG_REG, (byte)0b00000000);
        // // Enable interrupt only for wake on motion.
        // mDevice.writeRegByte(INT_ENABLE_REG, (byte)0b01000000);
        // // Enable Wake-on-Motion detection logic.
        // mDevice.writeRegByte(ACCEL_INTEL_CTRL, (byte)0b10000000);

        // Disable temperature sensor.
        mDevice.writeRegByte(PWR_MGMT_1, (byte)0b00001000);
        // Enable accelerometer and gyro.
        mDevice.writeRegByte(PWR_MGMT_2, (byte)0b00000000);

        Log.d(TAG, "config: the MPU 6500 sensor is configured.");
    }
}
