#!/usr/bin/env bash
set -euo pipefail

ADB_BIN="${ADB_BIN:-adb}"

echo "[stabilize] waiting for device..."
"${ADB_BIN}" wait-for-device

echo "[stabilize] waiting for boot completion..."
until [ "$("${ADB_BIN}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
  sleep 2
done

echo "[stabilize] waiting for boot animation to stop..."
until [ "$("${ADB_BIN}" shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r')" = "stopped" ]; do
  sleep 2
done

echo "[stabilize] disabling system animations..."
"${ADB_BIN}" shell settings put global window_animation_scale 0 || true
"${ADB_BIN}" shell settings put global transition_animation_scale 0 || true
"${ADB_BIN}" shell settings put global animator_duration_scale 0 || true

echo "[stabilize] reducing background load..."
for pkg in \
  com.google.android.apps.youtube.music \
  com.google.android.youtube \
  com.google.android.apps.messaging \
  com.google.android.googlequicksearchbox \
  com.google.android.as
do
  "${ADB_BIN}" shell pm disable-user --user 0 "${pkg}" >/dev/null 2>&1 || true
done

echo "[stabilize] verifying package manager health..."
"${ADB_BIN}" shell cmd package list packages >/dev/null

echo "[stabilize] emulator is ready."
