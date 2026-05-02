# Play Store Submission Checklist

Use this checklist before submitting your Mindroid-based app to the Google Play Store.

## App Identity

- [ ] Unique Application ID set in `app/build.gradle.kts` (not `dev.mindroid.host`)
- [ ] `versionCode` is 1 or higher
- [ ] `versionName` is set (e.g. `"1.0"`)
- [ ] App name, icon, and description updated

## Keystore & Signing

- [ ] Release keystore generated (see [RELEASE.md](RELEASE.md))
- [ ] `keystore.properties` file configured and NOT committed to git
- [ ] App builds successfully with `mindroid build release`
- [ ] APK/AAB is signed (verify with `apksigner verify app-release.apk`)

## Permissions Audit

- [ ] Only permissions actually used by the app are declared in AndroidManifest.xml
- [ ] Every `uses-permission` has a corresponding in-app usage
- [ ] Runtime permission requests have clear UI rationale strings
- [ ] Sensitive permissions reviewed (see [SENSITIVE_MODULES.md](SENSITIVE_MODULES.md))

## Sensitive API Compliance

- [ ] SMS access: completed Play Store SMS and Call Log access declaration form
- [ ] Contacts access: documented use case in app store listing
- [ ] Call-log access: completed policy review (extremely restrictive, almost always rejected)
- [ ] Camera/microphone: Privacy Policy URL provided in Play Console

## Security

- [ ] `allowBackup="false"` set if the app stores sensitive data
- [ ] ProGuard/R8 rules reviewed if minification is enabled
- [ ] No hardcoded secrets or API keys in source code
- [ ] Network traffic uses HTTPS only (no HTTP cleartext for production data)
- [ ] `android:exported` set correctly for all components

## Content Rating

- [ ] Content rating questionnaire completed in Play Console
- [ ] Age rating appropriate for target audience

## Store Listing

- [ ] App title and short description completed
- [ ] Full description written
- [ ] At least 2 screenshots uploaded per device type
- [ ] Feature graphic uploaded (1024x500 px)
- [ ] Privacy Policy URL set and accessible

## Testing

- [ ] Tested on API 29 (minimum supported)
- [ ] Tested on latest API level
- [ ] Pre-launch report reviewed in Play Console (Firebase Test Lab)
- [ ] Crash-free rate acceptable before full rollout

## Release

- [ ] Internal testing track promoted to closed/open before production
- [ ] Staged rollout planned (10% → 50% → 100%) for significant updates
- [ ] Rollback plan documented
