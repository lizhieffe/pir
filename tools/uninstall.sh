#!/bin/bash

connect_rpi.sh
adb uninstall com.example.android_things.pir.myapplication
adb uninstall com.android_things.sensor_experiment

# connect_pi.sh
# adb uninstall com.example.android_things.pir.myapplication
# adb install ../app/build/outputs/apk/debug/app-debug.apk
# adb reboot
