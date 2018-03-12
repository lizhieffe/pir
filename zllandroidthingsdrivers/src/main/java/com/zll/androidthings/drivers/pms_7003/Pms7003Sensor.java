package com.zll.androidthings.drivers.pms_7003;

import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.zll.androidthings.base.Constants;

import java.io.IOException;

/**
 * Created by lizhieffe on 1/23/18.
 *
 * Driver for PM7003 Air Quality Sensor.
 *
 * Probably it also works for PMS5003 and PMS6003 sensors.
 */

public class Pms7003Sensor {
    private static final String TAG = "PMS-7003 Sensor";

    private UartDevice mDevice;
    private String mPort;
    private Pms7003SensorDataParser mParser;

    private PeripheralManagerService mPioService;

    volatile private Pms7003SensorData mData = new Pms7003SensorData();

    public Pms7003Sensor() {
        this(Constants.RPI_3_UART_PORT);
        mParser = new Pms7003SensorDataParser();
    }

    public Pms7003Sensor(String port) {
        mPort = port;
    }

    public void startup() throws IOException {
        mPioService = new PeripheralManagerService();
        Log.d(TAG, "Pms7003Sensor.startup: UART bus list: "
                + mPioService.getUartDeviceList());
        connect();
    }

    public void shutdown() {
        if (mDevice != null) {
            try {
                mDevice.close();
            } catch (IOException e) {
                Log.e(TAG, "Pms7003Sensor.shutdown: ", e);
            } finally {
                mDevice = null;
            }
        }
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
                Log.e(TAG, "Pms7003Sensor.onUartDeviceDataAvailable: ", e);
            }
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.e(TAG, "Pms7003Sensor.onUartDeviceError: " + error);
        }
    };

    private void connect() throws IOException {
        mDevice = mPioService.openUartDevice(mPort);

        // TODO: not sure if these are the correct or best configs.
        mDevice.setBaudrate(9600);
        mDevice.setDataSize(8);
        mDevice.setParity(UartDevice.PARITY_NONE);
        mDevice.setStopBits(1);

        mDevice.registerUartDeviceCallback(onUart);
    }

    public Pms7003SensorData read() throws IOException {
        return mData;
    }

    private void readUartBuffer(UartDevice uart) throws IOException {
        final int maxCount = 256;
        byte[] buffer = new byte[maxCount];

        int count;  // number of bytes read
        count = uart.read(buffer, buffer.length);
        // Log.d(TAG, "Pms7003Sensor.readUartBuffer: read count = " + count);
        for (int i = 0; i < count; i++) {
            // Log.d(TAG, "Pms7003Sensor.readUartBuffer: i = " + i + ", count = " + count);
            // Log.d(TAG, "Pms7003Sensor.readUartBuffer: "
            //         + String.format("%02X ", buffer[i]));
            Pms7003SensorData result = mParser.parse(buffer[i]);
            if (result != null) {
                mData = result;
                // Log.e(TAG, "Pms7003Sensor.readUartBuffer: data ready");
                // Log.d(TAG, "Pms7003Sensor.readUartBuffer: data = " + result.toString());
            }
        }
    }
}
