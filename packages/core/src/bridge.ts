import type { MindroidBridge } from "./types.js";

export interface BridgeRequest {
  id: string;
  channel: string;
  method: string;
  args?: unknown;
}

export interface BridgeTransport {
  send(request: BridgeRequest): Promise<unknown>;
}

export class TransportBridge implements MindroidBridge {
  constructor(private readonly transport: BridgeTransport) {}

  async invoke<T = unknown>(channel: string, method: string, args?: unknown): Promise<T> {
    const response = await this.transport.send({
      id: crypto.randomUUID(),
      channel,
      method,
      args
    });
    return response as T;
  }
}
