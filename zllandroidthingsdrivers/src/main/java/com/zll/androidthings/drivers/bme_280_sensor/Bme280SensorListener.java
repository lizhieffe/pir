package com.zll.androidthings.drivers.bme_280_sensor;

/**
 * Created by lizhieffe on 1/20/18.
 */

public interface Bme280SensorListener {
    // Current temperature in degrees Celsius.
    public void onTemperatureData(float data);

    // Current barometric pressure in hPa units.
    public void onPressureData(float data);

    // Current relative humidity in RH percentage.
    // (100f means totally saturated air).
    public void onHumidityData(float data);
}
