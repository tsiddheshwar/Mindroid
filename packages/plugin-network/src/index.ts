import { defineModule } from "@mindroid/core";

export interface NetworkState {
  connected: boolean;
  transport: "wifi" | "cellular" | "ethernet" | "unknown";
  metered: boolean;
}

export function createNetworkPlugin() {
  return defineModule("network", ["network.status", "network.wifi"], (ctx) => {
    return {
      async getState(): Promise<NetworkState> {
        return ctx.bridge.invoke<NetworkState>("network", "getState");
      },
      async getWifiInfo(): Promise<{ ssid: string | null; ip: string | null }> {
        await ctx.permissions.ensure("android.permission.ACCESS_WIFI_STATE");
        return ctx.bridge.invoke("network", "getWifiInfo");
      }
    };
  });
}
