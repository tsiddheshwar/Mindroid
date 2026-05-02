import { describe, it, expect } from "vitest";
import { validateConfig, DEFAULT_POLICY } from "../config.js";
import { discoverPlugin } from "../plugin-registry.js";

describe("validateConfig", () => {
  it("accepts a fully valid config", () => {
    expect(() =>
      validateConfig({
        name: "my-app",
        plugins: ["@mindroid/plugin-device-info"],
        capabilities: ["device.info"],
        policy: { allowSensitiveModules: false, playStoreDisclosureConfirmed: false }
      })
    ).not.toThrow();
  });

  it("rejects missing name", () => {
    expect(() =>
      validateConfig({
        plugins: [],
        capabilities: [],
        policy: DEFAULT_POLICY
      })
    ).toThrow("name");
  });

  it("rejects non-array plugins", () => {
    expect(() =>
      validateConfig({
        name: "app",
        plugins: "bad",
        capabilities: [],
        policy: DEFAULT_POLICY
      })
    ).toThrow("plugins");
  });

  it("rejects missing policy", () => {
    expect(() =>
      validateConfig({
        name: "app",
        plugins: [],
        capabilities: []
      })
    ).toThrow("policy");
  });
});

describe("discoverPlugin", () => {
  it("returns metadata for a known plugin", () => {
    const meta = discoverPlugin("@mindroid/plugin-device-info");
    expect(meta?.capabilities).toContain("device.info");
  });

  it("returns undefined for an unknown plugin", () => {
    expect(discoverPlugin("@mindroid/does-not-exist")).toBeUndefined();
  });

  it("marks plugin-basic as sensitive", () => {
    expect(discoverPlugin("@mindroid/plugin-basic")?.sensitive).toBe(true);
  });
});
