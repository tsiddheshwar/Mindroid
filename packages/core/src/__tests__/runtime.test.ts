import { describe, it, expect, beforeEach } from "vitest";
import { MindroidRuntime } from "../runtime.js";
import { WebFallbackBridge } from "../web-fallback-bridge.js";
import { defineModule } from "../index.js";
import type { MindroidContext } from "../types.js";

function makeMockBridge(responses: Record<string, unknown> = {}) {
  return {
    async invoke<T>(channel: string, method: string): Promise<T> {
      const key = `${channel}.${method}`;
      if (key in responses) {
        return responses[key] as T;
      }
      return "granted" as T;
    }
  };
}

describe("MindroidRuntime", () => {
  it("starts with no modules", () => {
    const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });
    expect(runtime.getModuleNames()).toEqual([]);
  });

  it("registers a module and exposes its capabilities", () => {
    const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });
    const mod = defineModule("test", ["test.cap"], (_ctx: MindroidContext) => ({}));
    runtime.use(mod);
    expect(runtime.getModuleNames()).toContain("test");
    expect(runtime.getCapabilities()["test.cap"]).toBe("available");
  });

  it("audit log records module installation", () => {
    const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });
    const mod = defineModule("logger-test", ["log.cap"], (_ctx: MindroidContext) => ({}));
    runtime.use(mod);
    const log = runtime.getAuditLog();
    expect(log.some((r) => r.module === "logger-test" && r.action === "module-installed")).toBe(true);
  });

  it("registers multiple modules independently", () => {
    const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });
    const modA = defineModule("a", ["cap.a"], (_ctx: MindroidContext) => ({}));
    const modB = defineModule("b", ["cap.b"], (_ctx: MindroidContext) => ({}));
    runtime.use(modA).use(modB);
    expect(runtime.getModuleNames()).toEqual(["a", "b"]);
    expect(runtime.getCapabilities()["cap.a"]).toBe("available");
    expect(runtime.getCapabilities()["cap.b"]).toBe("available");
  });

  it("capability registry has available state after install", () => {
    const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });
    const mod = defineModule("cap-test", ["x.y", "x.z"], (_ctx: MindroidContext) => ({}));
    runtime.use(mod);
    const caps = runtime.getCapabilities();
    expect(caps["x.y"]).toBe("available");
    expect(caps["x.z"]).toBe("available");
  });
});

describe("PermissionManager via bridge", () => {
  it("calls the bridge and records the permission audit entry", async () => {
    const bridge = makeMockBridge({ "permissions.ensure": "granted" });
    const runtime = new MindroidRuntime({ bridge });
    const status = await runtime.context.permissions.ensure("android.permission.CAMERA");
    expect(status).toBe("granted");
    const log = runtime.getAuditLog();
    expect(log.some((r) => r.action === "ensure" && (r.details?.permission as string) === "android.permission.CAMERA")).toBe(true);
  });
});
