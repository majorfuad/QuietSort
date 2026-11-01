# T09 — Privacy exclusions and retention

## Controls
- **Save notifications locally** remains an explicit opt-in, OFF on fresh install.
- Exclusions are per Android package ID. Use the list of previously captured app packages or manually enter a package ID.
- Excluding a package updates the preference and deletes its existing encrypted notification records, including cascaded lifecycle events.
- Retention selection: 1, 7, 30 (default), 90 days.
- Retention is enforced at **each new POSTED write** and on cleanup. A daily WorkManager task prunes existing records when Android schedules it; exact timing is not guaranteed.
- **Run cleanup now** is available.
- **Erase all saved history** displays a confirmation dialog, disables capture, and deletes all notification and event rows.
- No INTERNET permission, remote API, analytics SDK, or plaintext message storage.

## Important privacy constraints
- Excluding an app does not prevent Android from delivering its notifications to the listener. It prevents saving those records.
- Android package identifiers, capture flags, and retention preferences are stored in app-private DataStore preferences; notification bodies remain only in encrypted SQLCipher Room.
- The system's app-specific notification settings determine which callbacks Android delivers. No access to notifications that Android redacts or blocks is guaranteed.
- The app is a developer build; do not distribute publicly until review of consent, privacy policy, permissions, and backup/restore behavior.
- Changing retention does not guarantee immediate secure erasure of deleted bytes from all flash storage, WAL files, or system-level backups. Android backup is disabled and no cloud backup is added.

## Verification
- Confirm an excluded package no longer contributes records to count, and post new notifications from it to verify it stays excluded.
- Re-enable an app; only future events should be stored, never old erased messages.
- Test retention drop and immediate cleanup.
- Erase everything then relaunch; saved counts and event counts should both be zero.
