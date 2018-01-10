#!/bin/bash

FILE=/storage/emulated/0/Android/data/com.android_things.sensor_experiment/files/Documents/sensor_data/2018-01-09

read -p "Are you sure? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
  connect_pi_2.sh
  adb shell rm "$FILE"
fi
