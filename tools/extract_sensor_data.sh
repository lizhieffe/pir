#!/bin/bash

# FILE=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/2018-01-09
# connect_pi_2.sh
# adb pull "$FILE" /tmp/pir_sensor_data.txt

# DIR=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/2018-01-09
# for FILENAME in "$DIR"/*; do
#   echo "$FILENAME"
# done

FILE=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/
adb pull "$FILE" /tmp/
