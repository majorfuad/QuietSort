# T04 — Listener lifecycle and ephemeral diagnostics

## Galaxy S25 verification
1. Check out `feat/T04-listener-lifecycle`, sync and install the debug build.
2. Open QuietSort, grant Android notification access if needed.
3. Status should say **Connected** once Android binds the service. Authorization alone is not a connection.
4. Trigger a notification from another app. The posted-event counter should increment.
5. Dismiss the notification. The removed-event counter should increment where Android reports removal.
6. Revoke listener access and return to QuietSort. Status must not claim it is connected solely because permission was previously granted.
7. Restart QuietSort's process: counters reset because they are deliberately volatile. Restarting the activity alone may not reset them.
8. Confirm no notification content is logged, displayed or persisted, and there is no INTERNET permission.

## Notes
- Some system notifications may not be delivered; notification ranking callbacks can occur independently of posted/removed callbacks.
- Android can kill/rebind the listener. A disconnected state is a lifecycle observation, not necessarily an error.
- No app-name/title/body capture until T05, no database before T06.
