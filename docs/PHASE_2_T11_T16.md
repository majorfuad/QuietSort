# QuietSort Phase 2 — Smart Inbox and Organization (T11–T16)

## Delivered implementation
- **T11 Inbox:** encrypted Room-backed chronological notification cards with title, preview, source package, timestamp, category and read/star/archive state. Inbox is the landing screen, Settings retains capture health, privacy and storage verification.
- **T12 Search and filters:** case-insensitive keyword search across app, title and content, app filter, category filter, relative date windows (24h, 7d, 30d), unread, favorites and archived views. Current implementation loads the latest 1,000 saved notifications into memory before filtering; see limitations.
- **T13 Offline classifier:** deterministic local categories Messages, Finance, Shopping, Promotions, Social, Work, System and Other. No remote model or connectivity.
- **T14 Rules:** add/remove user-defined rules by APP (exact package), TITLE (substring), or KEYWORD (title/body substring). Rules are saved in encrypted Room and override built-in classification; tie order is priority then ID.
- **T15 Duplicate grouping:** togglable grouping by app + normalized title/body + category + one-hour bucket; representative card displays group size. No records are destroyed. Bulk selection correctly includes every underlying grouped row.
- **T16 Actions:** mark read/unread, star/unstar, archive/restore, permanently delete with confirmation. Select individual cards or select all filtered records. Deleting rows cascades to notification lifecycle events.

## Database
- Version 3 adds `notifications.isRead` and `classification_rules`, using a non-destructive v2→v3 migration in the existing encrypted SQLCipher database.
- Existing v1→v2 migration remains; no fallback to plaintext SQLite.
- Retention, app exclusions and erase-all remain active from T09.
- Classification is computed on display so rule edits immediately affect historic notification categories.

## Test on Galaxy S25
1. Pull branch `feat/T11-T16-smart-inbox`; run `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
2. Install **over** the existing debug build without clearing app data. Verify previous encrypted notification counts and Settings / Verify encrypted storage.
3. Enable notification access and local capture. Generate notifications from several apps; open Smart Inbox.
4. Search for known title/body text. Test app, category, date, unread, starred and archive filters.
5. Create an APP rule to map a source package to a different category; confirm existing inbox records recategorize. Remove rule and verify fallback.
6. Test KEYWORD and TITLE rules, including a mixed-case match.
7. Send two identical notification contents under distinct IDs during the same hour; toggle Group duplicates and check group count.
8. Select a grouped card then archive / mark read / star; turn off grouping and verify every member changed.
9. Delete a selected card and confirm it disappears, including across app relaunch.
10. Use Settings to exclude an app and erase its history; confirm inbox reflects deletion. Change retention, run cleanup, verify old items disappear.
11. Check airplane mode, portrait and landscape, app background/foreground, no crashes and no INTERNET permission.

## Limitations before public release
- Filters/search operate on a capped 1,000-record stream; paging + indexed/FTS database search are planned in performance T24.
- Duplicate grouping is exact normalized text within the same hour, not semantic similarity.
- No in-notification reply or Android action invocation; actions modify QuietSort's saved records only.
- Only source package IDs are displayed; friendly application labels/icons planned for T22/T25.
- Rule priority is stored but not editable in this UI; rules are applied in insertion order at equal priority.
- No custom category editor or automatic external action execution.
- A combined T08–T10 branch is the parent of this Phase 2 branch and still awaits device acceptance. Phase 2 should not be merged into main until parent is tested/merged.
