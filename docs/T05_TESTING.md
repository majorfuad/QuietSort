# T05 — Notification normalization

## Scope
- Immutable, short-lived normalized representation from Android StatusBarNotification.
- Handles missing title/body/big text, empty identity, updates, grouping, flags, and truncates text.
- The listener performs normalization then immediately discards the result; **no notification content is persisted**.

## Test
1. `./gradlew :app:assembleDebug :app:testDebugUnitTest`
2. Confirm `NotificationNormalizerTest` passes.
3. On the S25, allow notification access and trigger a normal notification.
4. Confirm posted-event count increases and screen does not display contents.
5. Check merged manifest still has no INTERNET permission.

## Deferred
- Actual storage and lifecycle upserts: T06/T07.
- Robust Android framework edge-case handling beyond the pure mapper will be refined during integration.
