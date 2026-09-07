# Buck Manager - AI Development Guidelines

This file serves as a reference for any AI agent developing the Buck Manager app. It outlines the tech stack, architecture, database schema, and best practices specific to this project.

## Tech Stack
- **Framework:** Expo (v56) / React Native (v0.85)
- **Styling:** NativeWind (Tailwind CSS v3.4), supporting Light and Dark modes.
- **Navigation:** React Navigation v7 (Native Stack & Bottom Tabs).
- **Database:** Local SQLite (`expo-sqlite`).
- **State Management:** React Context API (Auth, Envelope, Theme, Alert).
- **Key Integrations:** 
  - `react-native-google-mobile-ads` for Ads & Monetization.
  - `expo-notifications` for reminders/alerts.
  - `react-native-android-widget` for the Home Screen Widget.
  - `expo-file-system` and Google Drive API for Cloud Backup.

## Project Structure
- `src/components/`: Reusable UI components (including Overlays and custom Modals).
- `src/context/`: State management (`AuthContext`, `EnvelopeContext`, `ThemeContext`, `AlertContext`).
- `src/database/`: SQLite database initialization and queries (`db.js`).
- `src/navigation/`: Routing setup (`AppNavigator.js`).
- `src/screens/`: Main application screens (`Dashboard`, `Transactions`, `Accountant`, `Login`).
- `src/services/`: External integrations (`GoogleDriveService`, `NotificationService`).
- `src/utils/`: Helper functions.
- `src/widget/`: Code for the Android Home Screen widget (`EnvelopesWidget.js`, `WidgetTask.js`).

## Database Schema (`buckmanager.db`)
Uses SQLite with WAL journaling mode.
1. **transactions**
   - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
   - `type` (TEXT) - e.g., 'expense', 'income'
   - `amount` (REAL)
   - `category` (TEXT) - Must map to an envelope ID.
   - `date` (TEXT) - ISO String
   - `description` (TEXT)
2. **settings**
   - `key` (TEXT PRIMARY KEY) - Stores non-sensitive configurations like `allocation_needs`, `envelopes_config`, `widget_config`, etc.
   - `value` (TEXT) - Stored as strings (including JSON arrays for `envelopes_config`).
   - *Note:* Sensitive credentials and tokens (`user_session`, `is_premium`, `ad_tickets`) are stored securely using `expo-secure-store` rather than SQLite.
3. **recurring_bills**
   - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
   - `name` (TEXT)
   - `amount` (REAL)
   - `category` (TEXT)
   - `day_of_month` (INTEGER)
   - `is_active` (INTEGER)
   - `last_processed` (TEXT) - Format 'YYYY-MM'
   - `created_at` (TEXT)

## Core Features & Systems
1. **Envelope System:** 
   - The core of the budgeting system is "Envelopes" (Needs, Wants, Savings).
   - Configuration is stored in the `settings` table under `envelopes_config` as a stringified JSON array.
2. **Gamification & Monetization:**
   - The app features streaks (`StreakIndicator.js`, `StreakRewardModal.js`) that reward users with "Customization Tickets" (`ad_tickets` in SecureStore).
   - `MonetizationOverlay.js` handles premium status and ad-based rewards.
3. **Deep Customization:**
   - The app has extensive customization for envelopes, backgrounds, headers, and widgets (`BackgroundEditorOverlay`, `HeaderCardEditorOverlay`, `WidgetEditorOverlay`).
   - Includes advanced visual effects like `ParticleLines.js` and `ParticleStarfall.js`.
4. **Cloud Sync:**
   - `GoogleDriveService.js` handles backing up and restoring the local SQLite `.db` file using Google Drive APIs via `expo-file-system`.

## Styling Rules
1. **NativeWind (v4):** The app uses NativeWind v4 with a `global.css` file mapped via Metro config.
2. **Theming & CSS Variables:** Do NOT use hardcoded colors or simple `dark:` variants for primary UI elements. The app uses CSS variables defined in `global.css` (e.g., `--color-primary`, `--color-background`, `--color-accent`) which are mapped in `tailwind.config.js`. 
   - Use semantic Tailwind classes like `bg-background`, `text-textLight`, `text-textDim`, `bg-primary`, `text-accent`.
3. **Dark Mode:** Dark mode is handled via the `.dark` class in `global.css`, which swaps the CSS variable values natively. The `ThemeContext` manages the current mode state.
4. **Aesthetics:** Prioritize modern, premium UI. Use gradients, glassmorphism (via `expo-blur`), and dynamic animations where appropriate (e.g., `react-native-reanimated`).

## Development Workflows
- **Adding Features:** Always ensure the SQLite database handles new features effectively. Use migrations or update `initDB()` in `src/database/db.js` if necessary, but keep backwards compatibility in mind.
- **Widgets:** If updating Envelopes, make sure to consider how the Android Widget updates (or trigger an update to the widget data).
- **Google Drive Sync:** Any schema migrations or DB locks must be handled carefully to avoid corrupting the Google Drive backup.

## Development Tracker
- Point to `DEV_TRACKER.md` in the project root.
- Point to the dev-tracker skill at `.agents/skills/dev-tracker/SKILL.md`.
- Any AI agent should read the tracker before starting work and update it after completing work.

## Code Conventions & Patterns
1. **Debounced DB Writes**: All UI→SQLite writes use `lodash.debounce` with 500ms delay via `useCallback`. Example: `saveEnvelopesToDB`, `saveBackgroundToDB`.
2. **SecureStore for Sensitive Data**: `is_premium`, `ad_tickets`, `premium_expiry_date`, `user_session` MUST use `expo-secure-store`. NEVER store these in SQLite.
3. **Overlay Pattern**: All sub-menus use the bottom-sheet overlay pattern (React Native `Modal` with `animationType="slide"`, semi-transparent backdrop `bg-black/50`).
4. **Live Preview**: All editor overlays have a sticky preview card at the top that updates in real-time via local React state.
5. **Context as Central Store**: `EnvelopeContext` is the central state manager. It handles envelopes, backgrounds, widgets, monetization, and streaks. Always go through context functions to modify state.
6. **Ref-synced State**: `envelopesRef` is kept in sync with `envelopes` state to ensure `updateEnvelope` always has the latest data even in stale closures.
7. **Share Customization Codes**: The app uses `BUCK-xxx` codes for sharing themes. These are Base64-encoded JSON preset objects. See `src/utils/presetUtils.js`.
8. **Post-Login Pipeline**: After OAuth login, the app runs a `postLoginSetup()` sequence (premium verification, data loading) before showing the dashboard.

## Critical Warnings (MUST READ)
⚠️ These are hard-learned lessons. Ignoring them WILL cause crashes:
1. **React Compiler + Widget**: The React Compiler causes infinite loops with `react-native-android-widget`. Widget components (`EnvelopesWidget.js`) MUST have `'use no memo';` at the top. Keep logic OUTSIDE the render cycle.
2. **Color Picker Libraries**: NEVER import `react-native-color-picker` or similar 3rd-party color pickers. They cause `SIGSEGV` crashes on Android release builds. Use the custom `SimpleColorPicker.js` (built with `@react-native-community/slider`).
3. **Base64 Image Prefix**: When passing base64 images to native widget `ImageWidget`, always ensure the string starts with `data:image/jpeg;base64,`. Without this prefix, the Java-side ImageWidget silently fails.
4. **expo-file-system/legacy**: This import is BANNED. Always use `expo-file-system` (standard). The legacy import causes bundling issues on Android 13+.
5. **WAL Checkpoint Before Backup**: Always call `checkpointDB()` before uploading to Google Drive. Without it, recent changes in the WAL file won't be included in the backup.
6. **Token Storage**: Google OAuth tokens MUST be in `expo-secure-store`, NOT in SQLite. The SQLite DB is backed up to Google Drive — tokens in the DB file would leak credentials.

## How to Continue Development
Step-by-step instructions for any AI:
1. Read `DEV_TRACKER.md` to understand current status and find your task.
2. Read this file (`AGENTS.md`) to understand the codebase architecture.
3. If unsure about a pattern, read the existing code — it's well-documented.
4. Mark your task as `[/]` in `DEV_TRACKER.md` when starting.
5. Follow existing patterns: NativeWind classes, Context functions, overlay modals.
6. After completing work, mark your task as `[x]` in `DEV_TRACKER.md` with a date.
7. If you introduce a new pattern, document it in this AGENTS.md file.
8. Check the `docs/` directory for additional Indonesian-language documentation.

## File Dependency Map
Document which files commonly need to be updated together:
- **Envelope changes** → `EnvelopeContext.js` + `DashboardScreen.js` + `WidgetTask.js` (widget refresh)
- **New overlay** → Create `src/components/XxxOverlay.js` + register in parent screen or `SettingsOverlay.js`
- **Auth flow changes** → `AuthContext.js` + `LoginScreen.js` + `App.js`
- **Theme/color changes** → `global.css` (CSS variables) + `tailwind.config.js` (mapping)
- **New setting key** → `EnvelopeContext.js` (load/save) + `db.js` (if default needed)
- **Monetization changes** → `EnvelopeContext.js` (state) + `MonetizationOverlay.js` (UI) + SecureStore keys
- **Widget changes** → `WidgetEditorOverlay.js` (editor UI) + `EnvelopesWidget.js` (render) + `WidgetTask.js` (data)
