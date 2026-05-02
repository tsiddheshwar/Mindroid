import { describe, it, expect } from "vitest";
import { createDeviceInfoPlugin } from "@mindroid/plugin-device-info";
import { createNetworkPlugin } from "@mindroid/plugin-network";
import { createCameraPlugin, createSensorPlugin, createNotificationPlugin } from "@mindroid/plugin-basic";
import { MindroidRuntime } from "../runtime.js";

function makeStubBridge(overrides: Record<string, unknown> = {}) {
  return {
    async invoke<T>(channel: string, method: string): Promise<T> {
      const key = `${channel}.${method}`;
      if (key in overrides) {
        return overrides[key] as T;
      }
      return "granted" as T;
    }
  };
}

describe("plugin-device-info", () => {
  it("exposes device.info capability", () => {
    const plugin = createDeviceInfoPlugin();
    const runtime = new MindroidRuntime({ bridge: makeStubBridge() });
    runtime.use(plugin);
    expect(runtime.getCapabilities()["device.info"]).toBe("available");
  });

  it("calls bridge on getInfo", async () => {
    const fakeInfo = { manufacturer: "Google", model: "Pixel", osVersion: "14", apiLevel: 34, abi: ["arm64-v8a"] };
    const plugin = createDeviceInfoPlugin();
    const runtime = new MindroidRuntime({ bridge: makeStubBridge({ "device.getInfo": fakeInfo }) });
    runtime.use(plugin);
    const info = await plugin.api().getInfo();
    expect(info.manufacturer).toBe("Google");
  });
});

describe("plugin-network", () => {
  it("exposes network.status and network.wifi capabilities", () => {
    const plugin = createNetworkPlugin();
    const runtime = new MindroidRuntime({ bridge: makeStubBridge() });
    runtime.use(plugin);
    expect(runtime.getCapabilities()["network.status"]).toBe("available");
    expect(runtime.getCapabilities()["network.wifi"]).toBe("available");
  });
});

describe("plugin-basic", () => {
  it("camera plugin registers camera.capture capability", () => {
    const plugin = createCameraPlugin();
    const runtime = new MindroidRuntime({ bridge: makeStubBridge() });
    runtime.use(plugin);
    expect(runtime.getCapabilities()["camera.capture"]).toBe("available");
  });

  it("sensor plugin registers accelerometer and gyroscope", () => {
    const plugin = createSensorPlugin();
    const runtime = new MindroidRuntime({ bridge: makeStubBridge() });
    runtime.use(plugin);
    const caps = runtime.getCapabilities();
    expect(caps["sensor.accelerometer"]).toBe("available");
    expect(caps["sensor.gyroscope"]).toBe("available");
  });

  it("notification plugin registers local notification capability", () => {
    const plugin = createNotificationPlugin();
    const runtime = new MindroidRuntime({ bridge: makeStubBridge() });
    runtime.use(plugin);
    expect(runtime.getCapabilities()["notification.local"]).toBe("available");
  });
});
