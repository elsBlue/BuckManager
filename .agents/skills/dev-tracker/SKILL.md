---
name: dev-tracker
description: |
  Manages the Buck Manager development roadmap and feature tracker (DEV_TRACKER.md).
  Use this skill whenever you are:
  - Starting work on a new feature or bugfix
  - Completing a task that is tracked in DEV_TRACKER.md
  - Adding new planned features or bugs to the tracker
  - Reviewing the current development status
  - Checking what's next on the roadmap
---

# Dev Tracker Skill

This skill governs how AI agents interact with the project's development tracker file located at **`DEV_TRACKER.md`** in the project root.

## Purpose

The `DEV_TRACKER.md` file is the **single source of truth** for the development roadmap of Buck Manager. It tracks:
- Planned features and their implementation status
- Known bugs and tech debt items
- Version milestones and release targets
- Feature priorities and dependencies

## When to Use

Activate this skill in any of these scenarios:
1. **Before starting work**: Read `DEV_TRACKER.md` to understand the current state and find the relevant task.
2. **After completing work**: Update the task status from `[ ]` → `[x]` and add a completion note with date.
3. **When discovering bugs**: Add a new entry under the appropriate section.
4. **When the user requests a new feature**: Add it to the tracker under the correct milestone/phase.
5. **During planning sessions**: Reference the tracker for context on what's done and what's pending.

## Status Notation

Use these status markers consistently:

| Marker | Meaning |
|--------|---------|
| `[ ]` | Not started |
| `[/]` | In progress |
| `[x]` | Completed |
| `[!]` | Blocked / Needs discussion |
| `[~]` | Deferred / Deprioritized |

## Update Rules

1. **Always read the tracker first** before modifying it. Never overwrite sections you didn't change.
2. **Add timestamps** when marking tasks complete: `[x] Task name — ✅ Done 2026-08-02`
3. **Never delete entries**. If a feature is cancelled, mark it `[~]` with a reason.
4. **Preserve order**. New items go at the bottom of their respective section unless they have a specific priority placement.
5. **Link to relevant files**. When completing a task, add links to the key files modified.
6. **Add sub-tasks** when a feature is complex. Indent with 2 spaces.

## File Location

```
e:\Users\Nyte\Documents\Android\BuckManagerApp\DEV_TRACKER.md
```

## Example Entry

```markdown
### Phase 2: Premium & Monetization
- [x] Migrate monetization data to SecureStore — ✅ Done 2026-07-15
  - Files: `src/context/EnvelopeContext.js`, `src/components/MonetizationOverlay.js`
- [/] Implement post-login premium verification
  - Check Google Play Billing for existing purchases after OAuth login
- [ ] Add Share Customization system
  - Export/import customization presets as shareable codes
```
