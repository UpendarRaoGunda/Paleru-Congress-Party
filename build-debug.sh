#!/usr/bin/env sh
set -eu
./gradlew :app:assembleDebug
printf '\nAPK: app/build/outputs/apk/debug/app-debug.apk\n'
