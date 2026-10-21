#!/usr/bin/env bash
set -uo pipefail

./gradlew test connectedAndroidTest --no-daemon
test_status=$?
if (( test_status != 0 )); then
  mkdir -p app/build/reports/device
  adb shell dumpsys window > app/build/reports/device/window.txt
  adb shell uiautomator dump /sdcard/window.xml
  adb pull /sdcard/window.xml app/build/reports/device/window.xml
  adb logcat -d > app/build/reports/device/logcat.txt
fi
exit "$test_status"
