# Buck Manager Development Tracker

## Core Features
- [x] Basic UI setup (Compose)
- [x] Room Database setup
- [x] Local snapshots / JSON export
- [x] Universal card customization (radius/border/padding/gradients/images)
- [x] Dashboard UI
- [x] Fund goal + homescreen widget (display-only)
- [x] Play Billing wiring (`premium_lifetime`) — requires Play Console product
- [x] Local / guest session (Google optional)
- [x] Honest premium copy (customization only; Watch Ad hidden)
- [x] Restore purchases in Settings + paywall
- [x] JSON export in Settings; Drive labeled Coming soon
- [x] Widget customizer wired from Settings
- [ ] Google Drive Cloud Sync (OAuth config still needed)
- [ ] AdMob rewarded ads (watch-ad path is stub, UI hidden)
- [ ] Streak / Customization Pass (not implemented in Kotlin app)
- [ ] Play Console product + internal testing purchase (blocked until developer account)

## Notes
- Do not trust `docs/archive/legacy-expo/*` for current architecture.

- [x] P1.10 CI: unit tests + lint gate APK upload (reports artifact on failure)
