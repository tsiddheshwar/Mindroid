export type PermissionStatus = "granted" | "denied" | "prompt";

export type CapabilityState = "available" | "unavailable" | "partial";

export interface MindroidBridge {
  invoke<T = unknown>(channel: string, method: string, args?: unknown): Promise<T>;
  subscribe?(channel: string, event: string, listener: (payload: unknown) => void): () => void;
}

export interface MindroidModule {
  name: string;
  capabilities: string[];
  install(context: MindroidContext): void;
}

export interface MindroidContext {
  bridge: MindroidBridge;
  audit: AuditLogger;
  permissions: PermissionManager;
  capabilityRegistry: CapabilityRegistry;
}

export interface AuditRecord {
  timestamp: string;
  module: string;
  action: string;
  details?: Record<string, unknown>;
}

export interface AuditLogger {
  log(record: AuditRecord): void;
  list(): AuditRecord[];
}

export interface PermissionManager {
  ensure(permission: string): Promise<PermissionStatus>;
}

export interface CapabilityRegistry {
  set(name: string, state: CapabilityState): void;
  get(name: string): CapabilityState | undefined;
  toJSON(): Record<string, CapabilityState>;
}
