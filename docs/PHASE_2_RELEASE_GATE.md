# Phase 2 acceptance gate

Status: **implementation staged, local compilation and device integration not independently verified**.

Required checks:
- `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest`
- Validate SQLCipher migration v2→v3 without deleting existing saved notification history.
- Validate old v1→v2→v3 migration path on a test installation.
- Confirm encryption verifier succeeds after migration.
- Validate listener capture still works and respects exclusions, retention and disabled capture.
- Run InboxEngineTest and SmartClassifierTest.
- Confirm rule persistence across app restart and recategorization of old records.
- Verify bulk operations over deduplicated groups.
- Verify deleting a notification cascades its event trail; clearing all history still works.
- Confirm no sensitive Logcat output and no INTERNET permission.
- Verify screens are usable on Galaxy S25 small/large font size.

Do not merge until testing passes. The Phase 2 PR is stacked on the T08–T10 branch.
