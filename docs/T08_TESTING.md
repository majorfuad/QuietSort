# T08 — Capture health and diagnostics

## Overview
Health panel shows permission state, explicit capture preference, listener connection, session-only callback counts, successful processing operations, storage failures, queue drops and last activity timestamps. No package names, notification keys, titles, bodies, sender data or content are logged.

Health classification precedence:
1. Missing notification access
2. Capture paused by user
3. Listener disconnected
4. Processing failures or queue drops in current process
5. Connected with no observed errors

A healthy listener does **not** prove Android delivered all notifications. Counters are process-local and reset on process restart. Persisted encrypted DB counts remain across restarts.

## Galaxy S25 test
1. Checkout branch feat/T08-capture-health.
2. Run `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
3. Reinstall without clearing data.
4. Revoke notification access: expect **Notification access required**.
5. Grant access, turn capture OFF: expect **Capture paused**.
6. Turn capture ON with connected listener: expect **Capture running** (unless this process recorded errors).
7. Trigger a notification from another app; posted count and last-observed timestamp should update.
8. Dismiss the notification; removed count updates where Android delivers callback.
9. Verify successful processing count/last-saved timestamp updates, and encrypted DB counters are retained across app relaunch.
10. Check no notification content is shown in diagnostics, no INTERNET permission and no sensitive Logcat output.

## Known limits
- Listener bind/unbind is controlled by Android, so initial status may temporarily say disconnected.
- No automated error injection exposed in production UI.
- Counters measure our callbacks/processing attempts rather than end-to-end notification coverage.
