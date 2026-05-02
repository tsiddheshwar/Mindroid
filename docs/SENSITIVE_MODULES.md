# Sensitive Module Policy

Mindroid classifies the following plugin capabilities as sensitive because they access personal or private user data and are subject to additional Google Play policy requirements.

## Sensitive Capabilities Summary

| Capability | Plugin | Play Policy Required |
|---|---|---|
| `sms.send` | `@mindroid/plugin-basic`, `@mindroid/plugin-sms` | Yes – SMS & Call Log policy |
| `sms.read` | `@mindroid/plugin-basic`, `@mindroid/plugin-sms` | Yes – SMS & Call Log policy |
| `contacts.read` | `@mindroid/plugin-basic`, `@mindroid/plugin-contacts` | Yes – Privacy disclosure |
| `contacts.write` | `@mindroid/plugin-basic`, `@mindroid/plugin-contacts` | Yes – Privacy disclosure |
| `calllog.read` | `@mindroid/plugin-contacts` | Yes – SMS & Call Log policy |
| `biometrics.authenticate` | `@mindroid/plugin-basic` | Biometric data notice |
| `location.gps` | `@mindroid/plugin-basic` | Precise location disclosure |

## Build-time Guard

The Mindroid CLI blocks release and AAB builds when sensitive plugins are present and the policy fields are not confirmed:

```json
{
  "policy": {
    "allowSensitiveModules": false,
    "playStoreDisclosureConfirmed": false
  }
}
```

Set both to `true` only after completing the required policy review steps below.

## SMS and Call Log

Google Play requires all apps using `READ_SMS`, `RECEIVE_SMS`, `SEND_SMS`, or `READ_CALL_LOG` to:

1. Submit a **Permissions Declaration Form** explaining why the default SMS/dialer app cannot perform the same function.
2. Demonstrate that the core use case of the app requires these permissions.
3. Get explicit approval from Play's policy team.

Most apps should not request these permissions. Use alternative flows:
- Use the **SMS Intent** to open the default SMS app pre-filled with recipient and message.
- Use the **Phone Intent** to dial instead of reading call logs.

Only request `READ_SMS` / `SEND_SMS` if you are building a dedicated messaging application.

## Contacts

Apps using `READ_CONTACTS` or `WRITE_CONTACTS` must:

1. Declare use in the Play Console Privacy Policy.
2. Present a clear in-app explanation before the first runtime permission request.
3. Not sell or share contact data to third parties.

## Location

Apps using `ACCESS_FINE_LOCATION` must:

1. Declare use case in the Play Console listing.
2. Present a clear rationale dialog before requesting the permission at runtime (Mindroid does this via `requestPermission`).
3. Never request background location unless the core use case requires it.

## Biometrics

Biometric data is never stored or transmitted by the default Mindroid implementation. The BiometricPrompt API is used which keeps all data on-device. Inform users in your privacy policy that biometric data remains on device.

## Disabling Sensitive Modules by Default

Contacts and call-log plugins are disabled by default. To include them:

```bash
mindroid plugin add @mindroid/plugin-contacts
```

This adds the plugin to `mindroid.config.json` and marks the config as sensitive, requiring policy confirmation before release builds succeed.
