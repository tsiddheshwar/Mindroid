import { defineModule } from "@mindroid/core";

export interface DeviceInfo {
  manufacturer: string;
  model: string;
  osVersion: string;
  apiLevel: number;
  abi: string[];
}

export function createDeviceInfoPlugin() {
  return defineModule("device-info", ["device.info"], (ctx) => {
    return {
      async getInfo(): Promise<DeviceInfo> {
        const info = await ctx.bridge.invoke<DeviceInfo>("device", "getInfo");
        ctx.audit.log({
          timestamp: new Date().toISOString(),
          module: "device-info",
          action: "getInfo"
        });
        return info;
      }
    };
  });
}
