import type {
  AuditLogger,
  AuditRecord,
  CapabilityRegistry,
  CapabilityState,
  MindroidBridge,
  MindroidContext,
  MindroidModule,
  PermissionManager,
  PermissionStatus
} from "./types.js";

class InMemoryAuditLogger implements AuditLogger {
  private readonly records: AuditRecord[] = [];

  log(record: AuditRecord): void {
    this.records.push(record);
  }

  list(): AuditRecord[] {
    return [...this.records];
  }
}

class DefaultPermissionManager implements PermissionManager {
  constructor(private readonly bridge: MindroidBridge, private readonly audit: AuditLogger) {}

  async ensure(permission: string): Promise<PermissionStatus> {
    const status = await this.bridge.invoke<PermissionStatus>("permissions", "ensure", { permission });
    this.audit.log({
      timestamp: new Date().toISOString(),
      module: "permissions",
      action: "ensure",
      details: { permission, status }
    });
    return status;
  }
}

class InMemoryCapabilityRegistry implements CapabilityRegistry {
  private readonly capabilities = new Map<string, CapabilityState>();

  set(name: string, state: CapabilityState): void {
    this.capabilities.set(name, state);
  }

  get(name: string): CapabilityState | undefined {
    return this.capabilities.get(name);
  }

  toJSON(): Record<string, CapabilityState> {
    return Object.fromEntries(this.capabilities.entries());
  }
}

export interface MindroidRuntimeOptions {
  bridge: MindroidBridge;
  autoPermissionRequest?: boolean;
}

export class MindroidRuntime {
  readonly context: MindroidContext;
  private readonly modules = new Map<string, MindroidModule>();

  constructor(options: MindroidRuntimeOptions) {
    const audit = new InMemoryAuditLogger();
    this.context = {
      bridge: options.bridge,
      audit,
      permissions: new DefaultPermissionManager(options.bridge, audit),
      capabilityRegistry: new InMemoryCapabilityRegistry()
    };
  }

  use(module: MindroidModule): this {
    module.install(this.context);
    this.modules.set(module.name, module);
    for (const capability of module.capabilities) {
      if (!this.context.capabilityRegistry.get(capability)) {
        this.context.capabilityRegistry.set(capability, "available");
      }
    }
    this.context.audit.log({
      timestamp: new Date().toISOString(),
      module: module.name,
      action: "module-installed",
      details: { capabilities: module.capabilities }
    });
    return this;
  }

  getModuleNames(): string[] {
    return [...this.modules.keys()];
  }

  getCapabilities(): Record<string, CapabilityState> {
    return this.context.capabilityRegistry.toJSON();
  }

  getAuditLog() {
    return this.context.audit.list();
  }
}
