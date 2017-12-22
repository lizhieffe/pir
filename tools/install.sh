#!/bin/bash

cd ..
./gradlew installDebug
cd tools

# connect_pi.sh
# adb uninstall com.example.android_things.pir.myapplication
# adb install ../app/build/outputs/apk/debug/app-debug.apk
# adb reboot
