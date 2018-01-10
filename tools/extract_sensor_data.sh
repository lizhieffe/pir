#!/bin/bash

FILE=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/2018-01-09

connect_pi_2.sh
adb pull "$FILE" /tmp/pir_sensor_data.txt
# adb shell rm "$FILE"
