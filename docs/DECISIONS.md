# Architecture decisions

- ADR-001: Start as a single Android application module with feature-oriented packages.
- ADR-002: Do not request INTERNET permission.
- ADR-003: Delay real notification-content storage until encrypted Room-compatible SQLite has been tested.
- ADR-004: Start with deterministic offline rules; no ML in MVP.
- ADR-005: Do not use AccessibilityService or promise universal notification blocking.
