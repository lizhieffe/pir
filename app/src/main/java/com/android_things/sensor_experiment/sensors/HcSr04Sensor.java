package com.android_things.sensor_experiment.sensors;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

/**
 * Driver for the HC-SR04 proximity sensor. The working distance of the sensor
 * is about 2cm - 4m.
 */
public class HcSr04Sensor extends GpioCallback implements MotionSensor {
    public static final int SENSOR_READ_INTERVAL_MS = 250;

    public class Event {
        public float distance = -1;  // cm
    }

    public interface Listener {
        public void onEvent(Event event);
    }

    private static final String ECHO_PIN = "BCM20";
    private static final String TRIGGER_PIN = "BCM21";

    private Gpio mEchoGpio;
    private Gpio mTriggerGpio;

    private Handler mEchoGpioCallbackHandler;
    private Handler mSensorSampler;

    private List<Listener> mListeners = new ArrayList<>();

    volatile private Event mEvent;

    @Override
    public void startup() {
        PeripheralManagerService service = new PeripheralManagerService();

        Log.d(TAG, "Available GPIOS: " + service.getGpioList());

        try {
            mEchoGpio = service.openGpio(ECHO_PIN);
            mEchoGpio.setDirection(Gpio.DIRECTION_IN);
            mEchoGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mEchoGpio.setActiveType(Gpio.ACTIVE_HIGH);

            HandlerThread echoGpioCallbackThread
                    = new HandlerThread("EchoGpioCallbackThread");
            echoGpioCallbackThread.start();
            mEchoGpioCallbackHandler = new Handler(echoGpioCallbackThread.getLooper());
            mEchoGpio.registerGpioCallback(this, mEchoGpioCallbackHandler);

            mTriggerGpio = service.openGpio(TRIGGER_PIN);
            mTriggerGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            HandlerThread sensorSamplerThread
                    = new HandlerThread("ProximitySensorSamplerThread");
            sensorSamplerThread.start();
            mSensorSampler = new Handler(sensorSamplerThread.getLooper());
            mSensorSampler.post(mSensorSamplerTriggerRunnable);
        } catch (IOException e) {
            Log.d(TAG, "HcSr04Sensor.startup: Error on PeripheralIO API", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            mListeners.clear();
            mEchoGpio.close();
            mTriggerGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot close GPIO bus: ", e);
        }
    }

    // TODO: refactor this method to reuse code from the async version.
    // Returned value unit is cm.
    float readDistanceSync() throws IOException {
        return mEvent.distance;
    }

    public void readDistanceAsync() throws IOException, InterruptedException {
        if (mTriggerGpio == null || mEchoGpio == null) {
            return;
        }

        // Just to be sure, set the trigger first to false
        mTriggerGpio.setValue(false);
        Thread.sleep(0, 2000);  // 2000 nano seconds

        // Hold the trigger pin HIGH for at least 10 us
        mTriggerGpio.setValue(true);
        Thread.sleep(0,10000); //10 microsec

        mTriggerGpio.setValue(false);  // reset the trigger
    }

    public void addListener(Listener listener) {
        if (mListeners != null) {
            mListeners.add(listener);
        }
    }

    volatile boolean mIsEchoStart = false;
    volatile private long mEchoStartMs = 0;
    volatile long mEchoEndMs = 0;

    @Override
    public boolean onGpioEdge(Gpio gpio) {
        try {
            if (gpio.getValue()) {
                mEchoStartMs = System.nanoTime();
                mIsEchoStart = true;
            } else if (!gpio.getValue()){
                mEchoEndMs = System.nanoTime();

                float distance = 0;
                if (mIsEchoStart) {
                    distance
                            = (float) (((mEchoEndMs - mEchoStartMs) / 1000.0) / 58.23); //cm
                } else {
                    distance = -1;
                }

                mIsEchoStart = false;
                Event event = new Event();
                event.distance = distance;
                mEvent = event;
            }
        } catch (IOException e) {
            Log.e(TAG, "HcSr04Sensor.onGpioEdge: cannot read GPIO: ", e);
        }
        return true;
    }

    private Runnable mSensorSamplerTriggerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                readDistanceAsync();
                mSensorSampler.postDelayed(mSensorSamplerTriggerRunnable,
                        SENSOR_READ_INTERVAL_MS);
            } catch (IOException e) {
                Log.e(TAG, "HcSr04Sensor.run: Error on PeriphalIO API: ", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "HcSr04Sensor.run: Error on PeriphalIO API: ", e);
            }
        }
    };
}
