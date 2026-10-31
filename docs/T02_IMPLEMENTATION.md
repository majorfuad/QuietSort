# T02 — Core architecture and dependencies

## Added
- Hilt app setup using `@HiltAndroidApp` and `@AndroidEntryPoint`.
- Dependency definitions for Room, Navigation Compose, DataStore, Coroutines, Lifecycle.
- Scoped DataStore implementation for privacy settings with capture defaulting to **false**.

## Deliberately deferred
- No Room database instance: plaintext notification persistence is prohibited until T06 encryption decisions/tests.
- No Android notification listener enabled before T03 onboarding.
- No Internet permission or telemetry SDK.

## Verification
1. Ensure Android Studio uses the Gradle wrapper and JBR 21.
2. Run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest`.
3. Confirm the merged manifest contains no `android.permission.INTERNET`.
4. Verify app opens with the starter screen.

**Status:** source implemented; CI/on-device build not verified yet.
