# Play Billing setup (BM3)

Product ID in code: `premium_lifetime` (one-time in-app product).

## Play Console
1. App with applicationId `com.buckmanager.app`
2. Monetize with Play → Products → In-app products → create `premium_lifetime`
3. Set price (e.g. IDR 15.000), activate
4. Upload a signed build to internal testing (Billing needs Play)
5. Add license testers for sandbox buys

## App behavior
- Purchase button opens Play Billing flow
- Owned purchases restored on startup via `BillingManager.refreshPurchases()`
- Settings and the paywall have **Restore purchases**
- Rewarded-ad unlock is hidden until AdMob is wired (`watchAd()` remains a stub)
- Premium copy only promises card/theme customization — not Drive or "all features"

## Local debug
Sideloaded debug APKs often cannot talk to Play Billing. Use an internal-test track install for real purchase tests.
