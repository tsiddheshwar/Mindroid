import type { MindroidBridge } from "./types.js";

export class WebFallbackBridge implements MindroidBridge {
  async invoke<T = unknown>(channel: string, method: string, args?: unknown): Promise<T> {
    throw new Error(
      `Capability unavailable in this environment: ${channel}.${method} (${JSON.stringify(args ?? {})})`
    );
  }
}
