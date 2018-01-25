package com.android_things.sensor_experiment;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.TextView;

import com.android_things.sensor_experiment.base.Features;
import com.android_things.sensor_experiment.detectors.AirQualityDetector;
import com.android_things.sensor_experiment.detectors.AmbientLightDetector;
import com.android_things.sensor_experiment.detectors.GestureDetector;
import com.android_things.sensor_experiment.controllers.AmbientLightIlluminanceController;
import com.android_things.sensor_experiment.controllers.DetectionController;
import com.android_things.sensor_experiment.controllers.GestureController;
import com.android_things.sensor_experiment.logger.MicAmplitudeLogger;
import com.android_things.sensor_experiment.pir.sensor_test.R;
import com.android_things.sensor_experiment.sensors.SensorRegistry;

import java.io.IOException;
import java.util.List;

import static com.android_things.sensor_experiment.base.Constants.TAG;

public class MainActivity extends Activity {

    private Context mContext;

    private AmbientLightDetector mAmbientLightDetector;
    private AirQualityDetector mAirQualityDetector;
    private GestureDetector mGestureDetector;

    private SensorManager mSensorManager;


    private AudioRecord mAudioRecord;
    private static final int SAMPLE_RATE = 44100;
    private static final int ENCODING_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_FORMAT = AudioFormat.CHANNEL_IN_MONO;
    private final int mBufferSize = AudioRecord
            .getMinBufferSize(SAMPLE_RATE, CHANNEL_FORMAT, ENCODING_FORMAT);
    private Handler mAudioRecordHandler = null;
    private HandlerThread mAudioRecordHandlerThread = null;
    private Runnable mAudioRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[10];
            mAudioRecord.read(buffer, 0, 10, AudioRecord.READ_BLOCKING);
            // Log.d(TAG, "MainActivity.run: read 10 bytes");
            mAudioRecordHandler.post(mAudioRunnable);
        }
    };

    private MediaRecorder mMediaRecorder;
    private Runnable mMediaRunnable = new Runnable() {
        @Override
        public void run() {
            int maxAmplitude = mMediaRecorder.getMaxAmplitude();
            mMicAmplitudeLogger.onData(maxAmplitude);
            mAudioRecordHandler.postDelayed(mMediaRunnable, 1000);
        }
    };
    private MicAmplitudeLogger mMicAmplitudeLogger;



    SensorRegistry mSensorRegistry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity.onCreate: 00000000");
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        startSensorRegistry();

        maybeStartAmbientLightDetection();
        maybeStartAirQualityDetection();
        maybeStartGestureDetection();
        maybeStartAudioRecord();
    }

    @Override
    protected void onDestroy() {
        if (Features.AMBIENT_LIGHT_DETECTION_ENABLED) {
            mAmbientLightDetector.shutdown();
        }
        if (Features.AIR_QUALITRY_DETECTION_ENABLED) {
            mAirQualityDetector.shutdown();
        }
        if (Features.GESTURE_DETECTION_ENABLED) {
            mGestureDetector.shutdown();
        }
        mSensorRegistry.shutdown();


        if (Features.AUDIO_RECORD_ENABLED) {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }

        super.onDestroy();
    }

    private void maybeStartAirQualityDetection() {
        if (Features.AIR_QUALITRY_DETECTION_ENABLED) {
            try {
                mAirQualityDetector = new AirQualityDetector(mSensorManager);
                mAirQualityDetector.start();
            } catch (IOException e) {
                Log.e(TAG, "MainActivity.onCreate: ", e);
            }
        }

    }

    private void maybeStartAmbientLightDetection() {
        if (Features.AMBIENT_LIGHT_DETECTION_ENABLED) {
            mAmbientLightDetector = new AmbientLightDetector(mSensorManager);
            mAmbientLightDetector.addListener(new AmbientLightIlluminanceController(
                    (TextView) findViewById(R.id.ambient_light_value_text_view)));
            mAmbientLightDetector.start();
        }

    }

    private void maybeStartGestureDetection() {
        if (Features.GESTURE_DETECTION_ENABLED) {
            mGestureDetector = new GestureDetector(mSensorManager);
            GestureController gestureIndicator = new GestureController(
                    (TextView)findViewById(R.id.gesture_text_view));
            mGestureDetector.addListener(gestureIndicator);
            mGestureDetector.start();
        }
    }

    private void maybeStartAudioRecord() {
        Log.d(TAG, "MainActivity.maybeStartAudioRecord: 111111");
        if (Features.AUDIO_RECORD_ENABLED) {
            // try {
            //     mAudioRecord = new AudioRecord.Builder()
            //             .setAudioSource(MediaRecorder.AudioSource.MIC)
            //             .setAudioFormat(new AudioFormat.Builder()
            //                     .setEncoding(ENCODING_FORMAT)
            //                     .setSampleRate(SAMPLE_RATE)
            //                     .setChannelMask(CHANNEL_FORMAT)
            //                     .build())
            //             .setBufferSizeInBytes(2 * mBufferSize)
            //             .build();
            //     Log.d(TAG, "MainActivity.onCreate: starting audio record ...");
            //     mAudioRecord.startRecording();
            //     Log.d(TAG, "MainActivity.onCreate: audio record started ...");
            // } catch (UnsupportedOperationException e) {
            //     Log.e(TAG, "MainActivity.onCreate: Did you add \"android.permission.RECORD_AUDIO\" permission to Manifest file?");
            //     Log.e(TAG, "MainActivity.onCreate: ", e);
            // }

            mAudioRecordHandlerThread = new HandlerThread("Audio Record Handler Thread");
            mAudioRecordHandlerThread.start();
            mAudioRecordHandler = new Handler(mAudioRecordHandlerThread.getLooper());
            // mAudioRecordHandler.post(mAudioRunnable);

            try {
                Log.e(TAG, "MainActivity.maybeStartAudioRecord: 222222");
                if (mMediaRecorder == null) {
                    mMediaRecorder = new MediaRecorder();
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mMediaRecorder.setOutputFile("/dev/null");
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                    Log.e(TAG, "MainActivity.maybeStartAudioRecord: 333333");

                    mMicAmplitudeLogger = new MicAmplitudeLogger(mContext);
                    mAudioRecordHandler.post(mMediaRunnable);
                }
            } catch (IOException e) {
                Log.e(TAG, "MainActivity.maybeStartAudioRecord: ", e);
            }
        }
    }

    private void startSensorRegistry() {
        mSensorRegistry = new SensorRegistry(this,
                mContext, mSensorManager,
                (TextView) findViewById(R.id.accel_text_view),
                (TextView) findViewById(R.id.gyro_text_view));
        mSensorRegistry.start();
    }
}
