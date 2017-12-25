#!/bin/bash

connect_rpi.sh
adb uninstall com.android_things.sensor_experiment
cd ..
./gradlew installDebug
cd tools
#adb shell am start -n com.android_things.sensor_experiment/com.android_things.sensor_experiment.MainActivity
adb reboot

# connect_pi.sh
# adb uninstall com.example.android_things.pir.myapplication
# adb install ../app/build/outputs/apk/debug/app-debug.apk
# adb reboot
