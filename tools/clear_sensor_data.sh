#!/bin/bash

FILE=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/2018-01-09
DIR=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/mpu*

read -p "Are you sure? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
  for filename in "${DIR}"; do
    adb shell rm "${filename}"
  done
fi

# read -p "Are you sure? " -n 1 -r
# echo    # (optional) move to a new line
# if [[ $REPLY =~ ^[Yy]$ ]]
# then
#   connect_pi_2.sh
#   adb shell rm "$FILE"
# fi
