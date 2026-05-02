/**
 * JS Engine interface for @mindroid/core.
 *
 * The bridge from this TypeScript side talks to the Android JsEngine interface.
 * App code should not import this directly – it is consumed by the MindroidRuntime.
 */

export type EngineEventHandler = (event: string, payload: unknown) => void;

export interface MindroidEngine {
  /**
   * Execute a bundle string as the entry point.
   */
  executeBundle(source: string): void;

  /**
   * Invoke a named top-level function that was defined in the bundle.
   */
  callFunction(name: string, args?: unknown[]): unknown;

  /**
   * Subscribe to events emitted from the native side.
   * Returns a teardown function.
   */
  onEvent(handler: EngineEventHandler): () => void;

  /**
   * Tear down and free engine resources.
   */
  destroy(): void;
}

/**
 * No-op stub used when running outside the Android host (e.g. during unit tests
 * or server-side rendering). Throws on any execution attempt so callers get
 * a clear error instead of a silent no-op.
 */
export class NoopEngine implements MindroidEngine {
  executeBundle(_source: string): void {
    throw new Error("NoopEngine: no JS engine available in this environment.");
  }

  callFunction(_name: string, _args?: unknown[]): unknown {
    throw new Error("NoopEngine: no JS engine available in this environment.");
  }

  onEvent(_handler: EngineEventHandler): () => void {
    return () => {};
  }

  destroy(): void {}
}
