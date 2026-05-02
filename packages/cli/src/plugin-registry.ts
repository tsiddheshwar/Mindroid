export interface PluginMetadata {
  name: string;
  capabilities: string[];
  sensitive?: boolean;
}

export const KNOWN_PLUGINS: Record<string, PluginMetadata> = {
  "@mindroid/plugin-device-info": {
    name: "@mindroid/plugin-device-info",
    capabilities: ["device.info"]
  },
  "@mindroid/plugin-network": {
    name: "@mindroid/plugin-network",
    capabilities: ["network.status", "network.wifi"]
  },
  "@mindroid/plugin-basic": {
    name: "@mindroid/plugin-basic",
    capabilities: [
      "camera.capture",
      "location.gps",
      "sensor.accelerometer",
      "sensor.gyroscope",
      "notification.local",
      "contacts.read",
      "contacts.write",
      "bluetooth.scan",
      "bluetooth.connect",
      "nfc.read",
      "nfc.write",
      "biometrics.authenticate",
      "background.service",
      "sms.send",
      "sms.read",
      "media.audio",
      "media.video"
    ],
    sensitive: true
  },
  "@mindroid/plugin-contacts": {
    name: "@mindroid/plugin-contacts",
    capabilities: ["contacts.read", "contacts.write", "calllog.read"],
    sensitive: true
  },
  "@mindroid/plugin-sms": {
    name: "@mindroid/plugin-sms",
    capabilities: ["sms.send", "sms.read"],
    sensitive: true
  }
};

export function discoverPlugin(name: string): PluginMetadata | undefined {
  return KNOWN_PLUGINS[name];
}
