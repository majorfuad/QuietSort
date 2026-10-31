# T07 — Encrypted notification history and event lifecycle

## Implementation
- User explicitly enables **Save notifications locally**. Default OFF.
- Notification listener forwards normalized data into a bounded, in-memory channel.
- A single background worker processes messages in order; callbacks do no database I/O.
- Repository serializes transactions and preserves existing favorites/archive state on updates.
- One active row per Android notification key; updates refresh that row, not duplicate it.
- Removal marks an active row removed and appends an event (does not delete history).
- Reposted keys after removal create a fresh row; every POSTED/UPDATED/REMOVED transition appends a separate event.
- Database schema v2; non-destructive migration v1 → v2 for prior T06 installations.
- Database stays SQLCipher-encrypted; no INTERNET permission or cloud services.
- Queue drops and DB failures are counted without logging notification content.
- In-memory queue drops entries on listener service destruction; not every event can be guaranteed on all devices.

## On-device checks (Galaxy S25)
1. Checkout `feat/T07-event-persistence` and run:
   `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest`
2. Install over T06 without clearing data. Tap **Verify encrypted storage** to confirm successful migration and reopen.
3. Grant listener access if necessary. Confirm **Save notifications locally** defaults OFF.
4. With capture OFF, send a notification from a second app: "Saved notifications" must not rise.
5. Turn capture ON and send a new notification. Saved notification count should increase once; event count rises.
6. Update the same Android notification ID (using a test notifier app). Saved count should stay constant while event count rises.
7. Dismiss a captured notification. Event count should rise, but saved count should remain constant.
8. Restart QuietSort and confirm counts persist while encrypted storage still verifies successfully.
9. Turn capture OFF and send additional notifications. No new rows or events should be saved.
10. Confirm no notification content is emitted to Logcat and manifest still has no INTERNET permission.

## Caveats
- This feature stores potentially sensitive message content at the user's explicit choice; do not publish without exclusion/retention controls (T09) and privacy disclosure.
- Existing notifications are not retroactively imported at enable time.
- Some notifications may be redacted or never delivered by Android.
- Removed callbacks for unseen notification keys are ignored.
- Repeated posted callbacks produce update events; there is no content hash deduplication yet.
- Android callback delivery is not guaranteed 100%; slow/failing database operations can result in bounded queue drops.
