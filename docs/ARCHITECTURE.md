# Architecture (current)

## Stack
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Persistence: Room (`transactions`, `settings` key-value JSON)
- Async: Kotlin Coroutines + StateFlow (`BuckViewModel`)
- Images: Coil; backgrounds persisted under `filesDir/backgrounds/`
- Auth / Drive: Google Sign-In + Drive API (sync still WIP — see `DEV_TRACKER.md`)
- Monetization: Google Play Billing (`premium_lifetime`) via `BillingManager`
- Widget: `GoalAppWidgetProvider` (display-only homescreen goal widget)

## Package
- `applicationId` / `namespace`: `com.buckmanager.app`

## Key modules
- `ui/` — screens, theme editors, modals
- `viewmodel/BuckViewModel.kt` — app state
- `data/` — Room DB + Drive sync worker
- `billing/BillingManager.kt` — Play Billing
- `widget/` — homescreen widget

## Build
- Debug APK via GitHub Actions on `main`
- Release: local `keystore.properties` + R8 (see `keystore.properties.example`)