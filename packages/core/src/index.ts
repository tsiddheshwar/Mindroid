export * from "./types.js";
export * from "./bridge.js";
export * from "./runtime.js";
export * from "./engine.js";
export * from "./ui.js";

import type { MindroidContext } from "./types.js";

export function defineModule<T>(name: string, capabilities: string[], factory: (ctx: MindroidContext) => T) {
  let api: T | undefined;

  return {
    name,
    capabilities,
    install(context: MindroidContext) {
      api = factory(context);
    },
    api() {
      if (!api) {
        throw new Error(`Module ${name} is not installed in runtime.`);
      }
      return api;
    }
  };
}
