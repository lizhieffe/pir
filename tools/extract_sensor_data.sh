#!/bin/bash

SUBFOLDER=sensor_data
FILE=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data
mkdir /tmp/"${SUBFOLDER}"
adb pull "$FILE" /tmp/"${SUBFOLDER}"
