# T06 — Encrypted Room database: verification

## Architecture
- Room 2.8.5 with SQLCipher for Android 4.17.0 via SupportOpenHelperFactory.
- Fresh 256-bit random database secret wrapped using non-exportable Android Keystore AES-256-GCM key.
- Encrypted secret stored in noBackupFilesDir. Android backup is disabled.
- If the database exists but its key disappears, open fails. No plaintext fallback.
- No real notification events are saved yet (T07).

## Galaxy S25 acceptance
1. Checkout feat/T06-encrypted-database.
2. Run `./gradlew :app:assembleDebug :app:testDebugUnitTest`.
3. Install. Open QuietSort and tap **Verify encrypted storage**.
4. Expect **Encrypted database opened** and **Plain SQLite header: absent**.
5. Close and relaunch. Repeat test; database should reopen using the same Keystore-wrapped secret and count increase.
6. Confirm notification content is **not** stored in this phase; only synthetic verification records are inserted.
7. Confirm the merged manifest has no INTERNET permission.
8. Optional: via adb inspect app-private database header (requires run-as debug build). It must not begin with `SQLite format 3`.

## Review required before merging
- Inspect dependency resolution for SQLCipher and Room compatibility.
- Validate key persistence across app restarts.
- Verify that uninstall/reinstall creates a fresh database, and no Android backup restores a ciphertext without its key.
- Manually test invalid/missing key: app should fail closed, not create an unencrypted database.
- Confirm an externally obtained copy of the database cannot be opened with standard unencrypted SQLite.
- Check there is no sensitive content in Logcat.
