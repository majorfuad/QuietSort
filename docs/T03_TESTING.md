# T03 — Permission onboarding verification

## Test on Galaxy S25
1. Install the app and confirm the initial text reads **Notification access not granted**.
2. Tap **Grant notification access**; Android settings should show QuietSort.
3. Enable QuietSort, acknowledge Android's sensitive notification warning, and return to the app.
4. Confirm the label changes to **Notification access granted** on resume.
5. Tap **Manage notification access**, revoke access and return; confirm it changes back.
6. Force-stop/relaunch and verify the setting is re-read.
7. Confirm no notifications are persisted, and the merged manifest has no INTERNET permission.

## Security and UX
- Notification access is a special system setting, not a runtime POST_NOTIFICATIONS permission.
- A listener component is registered but intentionally does not process/capture content.
- Display clearly that access may include sensitive alerts and messages.
- Notification access can be revoked at any time from Android settings.
