# T10 — Galaxy S25 integration and acceptance checklist

This is a **manual device acceptance milestone**. Implementation is staged; no claim that this milestone is validated until all tests are run on an S25.

## Build and install
1. Check out `feat/T09-T10-privacy-integration`.
2. Run `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
3. Install over the previous T07 build **without clearing app data** to validate Room v1→v2 migration and encrypted key persistence.
4. Verify the initial screen opens, no crashes, and **Verify encrypted storage** succeeds.
5. In Android Settings, grant listener access if not already granted.

## Listener, storage, diagnostics
- [ ] Listener toggles between connected and disconnected when access changes.
- [ ] Capture OFF does not persist notifications.
- [ ] Capture ON saves a new notification in SQLCipher.
- [ ] Update to same active notification changes content without raising saved-row count; event count grows.
- [ ] Dismissal appends a REMOVED event without deleting the historical row.
- [ ] Captured counts persist across process restart; session diagnostics reset.
- [ ] T08 health status updates correctly for access, capture paused, and connected scenarios.
- [ ] No exceptions or notification contents are logged to Logcat; no INTERNET permission.

## Exclusion, retention and erasure
- [ ] Capture a notification from two distinct apps.
- [ ] Exclude one app through the observed packages list; its historical rows and events disappear.
- [ ] New notifications from excluded app do not increase saved-row or event counts.
- [ ] Excluded app remains excluded after app restart.
- [ ] Re-enable excluded app; subsequent new notifications can be saved.
- [ ] Enter a valid package name manually, exclude it and see it persist.
- [ ] Invalid package name shows an error.
- [ ] Retention options 1/7/30/90 are selectable and persist across restart.
- [ ] **Run cleanup now** works and does not erase recent records.
- [ ] **Erase all saved history** cancel leaves data unchanged.
- [ ] Confirm erase clears saved notifications and lifecycle events; capture turns OFF.
- [ ] Closing and reopening app does not bring erased data back.

## Negative scenarios and performance
- [ ] Revoke Android notification access while capture was enabled; app reports access required.
- [ ] Turn capture OFF and post notifications; counts remain stable.
- [ ] Enable capture after a fresh install; do not backfill existing notifications.
- [ ] Rapidly post/update/dismiss notifications. App should remain responsive; review queue drops/storage failures.
- [ ] Rotate device and background/foreground app; controls should remain usable.
- [ ] Device airplane mode: app continues running and storing encrypted records locally.
- [ ] WorkManager retention maintenance is scheduled (its execution time is Android-managed).
- [ ] Optionally export a debug DB with `run-as` and verify ordinary SQLite cannot open it.
- [ ] Confirm no inadvertent App backup, analytics, or network permissions.

## Known scope limitations
- Not an end-user inbox, classifier or search experience yet.
- Android may redact or suppress notifications; not all callbacks can be guaranteed.
- WorkManager scheduling isn't exact. Manual cleanup handles immediate requests.
- Package exclusion UI shows package IDs rather than friendly installed-app names (Android package visibility limits).
- User-triggered deletion removes logical Room rows; physical flash-level secure erasure is not guaranteed.
- This PR is **not merged** pending device acceptance and full build validation.
