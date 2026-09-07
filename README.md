# Buck Manager (BM3)

Native Android envelope-budgeting app with deep UI customization and a homescreen **goal widget**.

**Stack:** Kotlin · Jetpack Compose · Material 3 · Room · Play Billing

**App ID:** `com.buckmanager.app`

## Features

### Dashboard
- Net worth with privacy toggle
- Income / expense summary cards
- Fund goal progress
- Budget envelopes (Needs / Wants / Savings + custom)
- Particle backgrounds (`none` / `lines` / `starfall`)
- Per-card customization: colors, images, radius, borders, padding, gradients

### Transactions
- Income and expense entries with categories
- History list with delete support

### Allocation
- Presets: Survival (60/10/30), Balanced (50/30/20), Aggressive Saver (40/10/50)
- Sliders with 100% total validation

### Widget
- Homescreen goal progress widget (display-only; tap opens the app)
- Styling follows the in-app fund goal theme

### Premium
- Lifetime unlock via **Google Play Billing** (`premium_lifetime`)
- Unlocks card/theme customization only (colors, photos, radius, particles)
- Restore purchases lives in Settings and on the paywall
- Rewarded-ad temporary unlock is **hidden** until AdMob is wired

### Backup
- **Export JSON** from Settings (primary backup today)
- Local snapshots in app storage
- Google Drive sync labeled Coming soon (OAuth still incomplete — see tracker)

### Sign-in
- Google Sign-In is optional
- **Continue locally** keeps envelopes and themes on this phone


## Build

### Requirements
- JDK 21
- Android SDK (compile/target 35, min 26)

### Debug
```bash
./gradlew assembleDebug
```

GitHub Actions also builds a debug APK on every push to `main`.

### Release
1. Copy `keystore.properties.example` → `keystore.properties` (gitignored)
2. Point it at your `release.keystore`
3. `./gradlew assembleRelease` (R8 minify enabled)

### Unit tests
```bash
./gradlew :app:testDebugUnitTest
```

## Docs
- [Architecture](docs/ARCHITECTURE.md)
- [Play Billing setup](docs/PLAY_BILLING_SETUP.md)
- [Dev tracker](DEV_TRACKER.md)
- Legacy Expo docs (archived, outdated): [docs/archive/legacy-expo](docs/archive/legacy-expo)

## License
MIT