# Create Android Apps With Mindroid

This guide shows the full flow: create app -> add plugins -> write JS code -> run on device -> build APK/AAB.

## Quickstart (Copy-Paste)

```bash
npm install -g mindroid
mindroid create my-app
cd my-app
mindroid plugin add @mindroid/plugin-device-info
mindroid plugin add @mindroid/plugin-network
mindroid plugin add @mindroid/plugin-basic
mindroid run
```

Build outputs:

```bash
mindroid build debug
mindroid build aab
```

## Prerequisites

- Node.js 20+
- JDK 17
- Android Studio (or Android command-line SDK tools)
- An Android emulator or physical device

## 1. Install Mindroid CLI

```bash
npm install -g mindroid
```

Or use npx without global install:

```bash
npx mindroid --help
```

## 2. Create a new app

```bash
mindroid create my-app
cd my-app
```

This generates:

- Android host project (Kotlin + Gradle)
- `mindroid.config.json` with initial plugins and policy fields
- `keystore.properties.example` for release signing setup

## 3. Add plugins for Android capabilities

List installed plugins:

```bash
mindroid plugin list
```

Add plugins:

```bash
mindroid plugin add @mindroid/plugin-device-info
mindroid plugin add @mindroid/plugin-network
mindroid plugin add @mindroid/plugin-basic
```

After adding plugins, Mindroid updates `mindroid.config.json` with discovered capabilities.

## 4. Use Mindroid modules in your JS app

Install runtime packages in your frontend app folder:

```bash
npm install @mindroid/core @mindroid/plugin-device-info @mindroid/plugin-network @mindroid/plugin-basic
```

Example usage:

```ts
import { MindroidRuntime, WebFallbackBridge } from "@mindroid/core";
import { createDeviceInfoPlugin } from "@mindroid/plugin-device-info";
import { createNetworkPlugin } from "@mindroid/plugin-network";
import { createCameraPlugin } from "@mindroid/plugin-basic";

const runtime = new MindroidRuntime({
  bridge: new WebFallbackBridge()
});

const device = createDeviceInfoPlugin();
const network = createNetworkPlugin();
const camera = createCameraPlugin();

runtime.use(device).use(network).use(camera);

async function bootstrap() {
  const info = await device.api().getInfo();
  const state = await network.api().getState();
  console.log("Device:", info);
  console.log("Network:", state);
}

bootstrap();
```

Note: `WebFallbackBridge` is for non-Android environments. On Android host, the native bridge handles real device calls.

## 5. Run on device or emulator

From the generated app directory:

```bash
mindroid run
```

This installs a debug build using Gradle.

## 6. Build APK and AAB

Debug APK:

```bash
mindroid build debug
```

Release APK + AAB:

```bash
mindroid build release
```

AAB only:

```bash
mindroid build aab
```

## 7. Configure release signing

Copy and edit signing config:

```bash
copy keystore.properties.example keystore.properties
```

Then fill values in `keystore.properties`:

```properties
storeFile=../release.keystore
storePassword=your-store-password
keyAlias=mindroid
keyPassword=your-key-password
```

## 8. Sensitive module policy (SMS/contacts/call-log)

If you use sensitive plugins/capabilities, update `mindroid.config.json` before release builds:

```json
{
  "policy": {
    "allowSensitiveModules": true,
    "playStoreDisclosureConfirmed": true
  }
}
```

Without these flags, Mindroid blocks release/AAB builds for safety.

## 9. App icon generation

Generate Android icon assets from PNG:

```bash
mindroid icon ./assets/app-icon.png ic_launcher
```

This creates mipmap PNG sets and adaptive icon XML resources.

## 10. Recommended release flow

1. Build and test debug APK on real devices
2. Build signed release AAB (`mindroid build aab`)
3. Follow Play checklist in `docs/PLAY_CHECKLIST.md`
4. Follow release details in `docs/RELEASE.md`
