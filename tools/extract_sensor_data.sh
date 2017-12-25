#!/bin/bash

FILE=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/pir_sensor_data.txt

connect_rpi.sh
adb pull "$FILE" /tmp/pir_sensor_data.txt
adb shell rm "$FILE"
