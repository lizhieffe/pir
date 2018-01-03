package com.android_things.sensor_experiment.sensors.zx_gesture;

import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Created by lizhieffe on 1/1/18.
 *
 * Driver for ZX gesture sensor using UART bus made by Sparkfun.
 *
 * Note: for unknown reason, the TXD pin on sensor should be connect to RXD pin
 * on RPI, and RXI pin on sensor should be connect to TXD pin on RPI.
 */

public class ZxGestureSensorUart {
    private final static String DEFAULT_UART_PORT = "UART0";

    private UartDevice mDevice;
    private String mPort;

    private UartParser mParser;
    private PeripheralManagerService mPioService;

    public ZxGestureSensorUart() {
        this(DEFAULT_UART_PORT);
    }

    public ZxGestureSensorUart(String port) {
        mPort = port;
    }

    public void startup() throws IOException {
        mParser = new UartParser();
        mPioService = new PeripheralManagerService();
        Log.d(TAG, "ZxGestureSensorI2C.startup: UART bus list: "
                + mPioService.getUartDeviceList());
        connect();
    }

    public void shutdown() {
       if (mDevice != null) {
           try {
               mDevice.close();
           } catch (IOException e) {
               Log.e(TAG, "ZxGestureSensorUart.shutdown: ", e);
           } finally {
               mDevice = null;
           }
       }
    }

    volatile private ZxGestureSensor.Gesture mLatestGesture
            = ZxGestureSensor.Gesture.UNKNOWN;

    public ZxGestureSensor.Gesture readGesture() {
        return mLatestGesture;
    }

    private void connect() throws IOException {
        mDevice = mPioService.openUartDevice(mPort);

        mDevice.setBaudrate(115200);
        mDevice.setDataSize(8);
        mDevice.setParity(UartDevice.PARITY_NONE);
        mDevice.setStopBits(1);

        mDevice.registerUartDeviceCallback(onUart);
    }

    /**
     * Callback invoked when there is data on the UART connection.
     */
    private final UartDeviceCallback onUart = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            try {
                readUartBuffer(uart);
            } catch (IOException e) {
                Log.e(TAG, "ZxGestureSensorUart.onUartDeviceDataAvailable: ", e);
            }
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.e(TAG, "ZxGestureSensorUart.onUartDeviceError: " + error);
        }
    };

    private void readUartBuffer(UartDevice uart) throws IOException {
        final int maxCount = 256;
        byte[] buffer = new byte[maxCount];

        int count;
        SensorResult result = null;
        while ((count = uart.read(buffer, buffer.length)) > 0) {
            // Log.d(TAG, "ZxGestureSensorUart.readUartBuffer: count = " + count);
            for (int i = 0; i < count; i++) {
                try {
                    result = mParser.parse(buffer[i]);
                } catch (InvalidByteException e) {
                    Log.e(TAG, "Invalid byte sequence encountered " +
                            "while reading from sensor connected to UART", e);
                }
                if (result != null) {
                    switch (result.resultType) {
                        case PEN_UP:
                            // mGestureDetector.penUp();
                            break;
                        case X_POS:
                            // mGestureDetector.setXpos(result.xPosition);
                            break;
                        case Z_POS:
                            // mGestureDetector.setZpos(result.zPosition);
                            break;
                        case GESTURE:
                            mLatestGesture = result.gesture;
                            // mGestureDetector.setGesture(result.gesture,
                            //        result.gestureParams);
                            break;
                        case RANGE:
                            // mGestureDetector.setRanges(result.rangeL, result.rangeR);
                            break;
                        case ID:
                            break;
                    }
                    result = null;
                }
            }
        }
    }

    static final class UartParser {
        /**
         * buffer for UART connection data
         */
        private int[] mIncomingBytes;
        /**
         * current position in buffer
         */
        private int mReadPosition = 0;


        public UartParser() {
            mIncomingBytes = new int[4];
            mReadPosition = 0;
        }

        /**
         * Message types for UART communication
         * @see <a href="https://cdn.sparkfun.com/assets/learn_tutorials/3/4/5/XYZ_Interactive_Technologies_-_ZX_SparkFun_Sensor_Datasheet.pdf">Datasheet</a>
         */
        private static final int PEN_UP = 0xFF;  // reflector moved out of range
        private static final int RANGES = 0xFE;  // x, z ranges. 2 byte, each range is [0, 240]
        private static final int X_COORD = 0xFA; // x coordinate. offset 120 [0, 240] -> [-120, 120]
        private static final int Z_COORD = 0xFB; // z coordinate. [0, 240]
        private static final int GESTURE_EVENT = 0xFC;  // gesture event. 2 byte. event code, param
        private static final int ID = 0xF1; // ID. 3 byte. sensor type, HW version, firmware version.

        /**
         * parse data from UART stream, byte by byte.
         *
         * @param b byte to be parsed next.
         */
        SensorResult parse(int b) throws InvalidByteException {
            SensorResult result = null;
            b = b & 0xFF;
            mIncomingBytes[mReadPosition] = b;

            if (mReadPosition == 0) {
                switch (mIncomingBytes[0]) {
                    case PEN_UP:
                        result = SensorResult.createPenUpResult();
                        break;
                    case RANGES:
                    case X_COORD:
                    case Z_COORD:
                    case GESTURE_EVENT:
                    case ID:
                        mReadPosition++;
                        result = SensorResult.createPendingResult();
                        break;
                    default:
                        throw new InvalidByteException(mIncomingBytes[0], 1, mIncomingBytes[0]);
                }
            } else if (mReadPosition == 1) {
                result = parseSecondByte();
            } else if (mReadPosition == 2) {
                result = parseThirdByte();
            } else { //last char of ID
                result = parseFourthByte();
            }
            if (result == null) {
                mReadPosition = 0;
                return null;
            }
            if (result.resultType == SensorResultType.PENDING) {
                return null;
            }
            return result;
        }

        /**
         * parses second byte. returns SensorResult for following messages:
         * X_COORD, Z_COORD
         * @return null when the data needs to be parsed further
         */
        private SensorResult parseSecondByte() throws InvalidByteException {
            SensorResult result = null;
            switch (mIncomingBytes[0]) {
                case RANGES:
                case GESTURE_EVENT:
                case ID:
                    mReadPosition++;
                    result = SensorResult.createPendingResult();
                    break;
                case X_COORD:
                    result = SensorResult.createXPositionResult(mIncomingBytes[1] - 120);
                    mReadPosition = 0;
                    break;
                case Z_COORD:
                    result = SensorResult.createZPositionResult(mIncomingBytes[1]);
                    mReadPosition = 0;
                    break;
                default:
                    throw new InvalidByteException(mIncomingBytes[0], 2, mIncomingBytes[1]);
            }
            return result;
        }

        /**
         * parses third byte. returns SensorResult for following messages:
         * RANGES, GESTURE_EVENT
         * @return null when the data needs to be parsed further
         */
        private SensorResult parseThirdByte() throws InvalidByteException{
            SensorResult result = null;
            switch (mIncomingBytes[0]) {
                case RANGES:
                    result = SensorResult.createRangeResult(mIncomingBytes[1], mIncomingBytes[2]);
                    mReadPosition = 0;
                    break;
                case GESTURE_EVENT:
                    ZxGestureSensor.Gesture gesture =
                            ZxGestureSensor.Gesture.getGesture(mIncomingBytes[1]);
                    if (gesture != null) {
                        result = SensorResult.createGestureResult(
                                gesture, mIncomingBytes[2]);

                    }
                    mReadPosition = 0;
                    break;
                case ID:
                    mReadPosition++;
                    result = SensorResult.createPendingResult();
                    break;
                default:
                    throw new InvalidByteException(mIncomingBytes[0], 3, mIncomingBytes[2]);
            }
            return result;
        }

        /**
         * parses fourth byte. called only for ID message
         * @return parsed result
         */
        private SensorResult parseFourthByte() throws InvalidByteException {
            SensorResult result = SensorResult.createIdResult(
                    mIncomingBytes[1], mIncomingBytes[2], mIncomingBytes[3]);
            mReadPosition = 0;
            return result;
        }
    }

    enum SensorResultType {
        X_POS, Z_POS, RANGE, PEN_UP, GESTURE, ID, PENDING
    }

    static final class SensorResult {
        SensorResultType resultType;
        int xPosition;
        int zPosition;
        int rangeL;
        int rangeR;
        ZxGestureSensor.Gesture gesture;
        int gestureParams;
        int sensorType;
        int hardwareVersion;
        int firmwareVersion;

        static SensorResult createPenUpResult() {
            return new SensorResult(SensorResultType.PEN_UP);
        }

        static SensorResult createXPositionResult(int xPosition) {
            SensorResult result = new SensorResult(SensorResultType.X_POS);
            result.xPosition = xPosition;
            return result;
        }

        static SensorResult createZPositionResult(int zPosition) {
            SensorResult result = new SensorResult(SensorResultType.Z_POS);
            result.zPosition = zPosition;
            return result;
        }

        static SensorResult createRangeResult(int rangeL, int rangeR) {
            SensorResult result = new SensorResult(SensorResultType.RANGE);
            result.rangeL = rangeL;
            result.rangeR = rangeR;
            return result;
        }

        static SensorResult createGestureResult(ZxGestureSensor.Gesture gesture, int param) {
            SensorResult result = new SensorResult(SensorResultType.GESTURE);
            result.gesture = gesture;
            result.gestureParams = param;
            return result;
        }

        static SensorResult createIdResult(int sensorType, int hardwareVersion, int firmwareVersion) {
            SensorResult result = new SensorResult(SensorResultType.ID);
            result.sensorType = sensorType;
            result.hardwareVersion = hardwareVersion;
            result.firmwareVersion = firmwareVersion;
            return result;
        }

        static SensorResult createPendingResult() {
            return new SensorResult(SensorResultType.PENDING);
        }

        private SensorResult(SensorResultType type) {
            resultType = type;
        }
    }

    static final class InvalidByteException extends RuntimeException {
        public InvalidByteException (int messageType, int position, int byteRead) {
            super("Serial Parsing error while parsing message type 0x" +
                    Integer.toHexString(messageType) + ", position: " +
                    position + ". Byte read: 0x" + Integer.toHexString(byteRead));
        }
    }
}
