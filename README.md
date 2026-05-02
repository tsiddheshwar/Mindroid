# Mindroid

Mindroid is a JS-first Android app framework and npm library ecosystem.

It is designed to let frontend developers build native Android apps with this stack:

frontend app -> Mindroid middleware/runtime -> Android OS

## Vision

- Minimal Android app code in the app layer, more JavaScript/TypeScript in feature code
- No WebView runtime requirement for app execution path
- Import Android capabilities as modular plugins
- Build output as Android APK/AAB

## Current Repository Layout

```
packages/
	core/                 # Runtime, bridge contracts, permission/audit base
	cli/                  # mindroid CLI (create, plugin add, run, build, icon)
	icon-builder/         # PNG -> Android icon assets/XML
	plugin-device-info/   # Device info plugin
	plugin-network/       # Network and Wi-Fi plugin
	plugin-basic/         # Camera/location/sensors/etc plugin factories
templates/
	android-host/         # Native Android host template (Kotlin + optional NDK)
examples/
	vanilla-app/
	react-app/
	angular-app/
```

## Implemented v0.2 Scaffold

- Plugin host and module registration in `@mindroid/core`
- Capability registry and graceful fallback bridge
- Strict permission gate hook + runtime audit log API
- Device info and network plugin packages
- Basic plugin factory package for camera, location, sensors, notifications, contacts, bluetooth, nfc, biometrics, background services, sms, and media
- CLI package (`mindroid`) with commands:
  - `mindroid create <app-name>`
  - `mindroid plugin add <plugin-name>`
  - `mindroid plugin list`
  - `mindroid run`
  - `mindroid build [debug|release|aab]`
  - `mindroid icon <input.png> [icon-name]`
- Config schema validation for `mindroid.config.json`
- Plugin capability auto-discovery and capability index generation in config
- Sensitive-module policy guard for release and AAB builds
- Icon builder support:
  - mipmap PNG density generation
  - adaptive icon XML generation
  - vector/bitmap XML helper generation
- Android host template includes native channel implementations for:
  - permissions
  - network state
  - file/storage read-write/list
  - location last-known position
  - local notifications (channel-aware)
  - camera availability and camera list
- Android template release signing support via `keystore.properties`
- Native C++ bridge placeholder (NDK-ready)

## Quick Start

1. Install dependencies

```bash
npm install
```

2. Build all packages

```bash
npm run build
```

3. Run CLI from workspace

```bash
npm run dev --workspace mindroid -- create my-app
```

4. Build Android app (from generated app folder)

```bash
mindroid build debug
mindroid build release
mindroid build aab
```

5. Configure release signing

```bash
copy keystore.properties.example keystore.properties
```

## App Development Guide

- Create and build Android apps with Mindroid: [docs/CREATE_ANDROID_APPS.md](docs/CREATE_ANDROID_APPS.md)
- Release signing and AAB flow: [docs/RELEASE.md](docs/RELEASE.md)
- Play Store checklist: [docs/PLAY_CHECKLIST.md](docs/PLAY_CHECKLIST.md)
- Sensitive capability policy: [docs/SENSITIVE_MODULES.md](docs/SENSITIVE_MODULES.md)

## Security Defaults

- Permission checks are routed through a central permission gate.
- Audit records are captured for sensitive module actions.
- Capability checks can signal unavailable or partial support on a device.
- Sensitive modules (SMS/contacts/call-log related) are blocked for release/AAB unless both fields are true in `mindroid.config.json`:
  - `policy.allowSensitiveModules`
  - `policy.playStoreDisclosureConfirmed`

## Open Work Needed To Reach Production

- Add real native implementations for remaining module channels (bluetooth, nfc, biometrics, media, background service orchestration)
- Add JS engine embedding implementation strategy (Hermes/JSC first, V8 pluggable)
- Implement native UI renderer contract for JS-driven widgets
- Add app-level runtime permission request UI flows
- Add emulator/device runner improvements in CLI
- Add full release docs including keystore generation and Play Console checklist
- Add test matrix (unit, integration, Android instrumented tests)
- Expand policy-safe handling docs for SMS/contacts/call-log capabilities