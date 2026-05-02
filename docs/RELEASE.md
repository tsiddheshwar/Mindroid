# Release Guide

## Prerequisites

- Android Studio Hedgehog or later (or command-line tools)
- JDK 17
- Node.js 20+
- mindroid CLI installed: `npm install -g mindroid` or `npx mindroid`

## 1 – Generate a release keystore

Run this once per app. Keep the keystore file and passwords secure – losing them means you can never update the app on Play Store.

```bash
keytool -genkey -v \
  -keystore release.keystore \
  -alias mindroid \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

You will be prompted for a keystore password, owner details, and a key password.

Store `release.keystore` outside your source control directory or add it to `.gitignore`.

## 2 – Configure keystore.properties

Copy the example file and fill in real values:

```
storeFile=../release.keystore
storePassword=your-store-password
keyAlias=mindroid
keyPassword=your-key-password
```

Never commit `keystore.properties` to source control. Add it to `.gitignore`.

## 3 – Configure policy for sensitive modules

If your app uses SMS, contacts, call-log, or similar sensitive plugins, open `mindroid.config.json` and set:

```json
{
  "policy": {
    "allowSensitiveModules": true,
    "playStoreDisclosureConfirmed": true
  }
}
```

Only set `playStoreDisclosureConfirmed` to `true` after completing the Play Store policy review process for your specific capabilities. See [SENSITIVE_MODULES.md](SENSITIVE_MODULES.md).

## 4 – Build a signed APK

```bash
mindroid build release
```

The signed APK will be at:

```
app/build/outputs/apk/release/app-release.apk
```

## 5 – Build an AAB (recommended for Play Store)

```bash
mindroid build aab
```

The AAB will be at:

```
app/build/outputs/bundle/release/app-release.aab
```

## 6 – Play Store upload

1. Sign in to the [Google Play Console](https://play.google.com/console)
2. Create a new app or select existing
3. Navigate to **Production > Releases > Create new release**
4. Upload the `.aab` file
5. Complete the store listing, content rating, and policy declarations
6. Submit for review

## 7 – App signing with Google Play App Signing (recommended)

Google Play App Signing lets Google manage your signing key after the first upload. This protects against keystore loss.

Enable it during the first release upload. Once enabled, you upload an upload key and Google re-signs before distributing.

## Versioning

Before each release, bump `versionCode` (must increase monotonically) and `versionName` in `app/build.gradle.kts`:

```kotlin
versionCode = 2
versionName = "1.1"
```

## CI/CD

Example GitHub Actions step for release builds:

```yaml
- name: Build release AAB
  run: |
    echo "storeFile=${{ secrets.KEYSTORE_PATH }}" > keystore.properties
    echo "storePassword=${{ secrets.STORE_PASSWORD }}" >> keystore.properties
    echo "keyAlias=${{ secrets.KEY_ALIAS }}" >> keystore.properties
    echo "keyPassword=${{ secrets.KEY_PASSWORD }}" >> keystore.properties
    npx mindroid build aab
```
