package com.android_things.sensor_experiment.utils;

import java.io.IOException;

/**
 * Created by lizhieffe on 12/30/17.
 * Interface for environment detector.
 */

public interface EnvDetector {
    public void start() throws IOException;
    public void shutdown();
}
