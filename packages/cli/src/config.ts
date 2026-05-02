import { readFile, writeFile } from "node:fs/promises";
import { existsSync } from "node:fs";

export interface MindroidPolicy {
  allowSensitiveModules: boolean;
  playStoreDisclosureConfirmed: boolean;
}

export interface MindroidConfig {
  name: string;
  plugins: string[];
  capabilities: string[];
  policy: MindroidPolicy;
}

export const DEFAULT_POLICY: MindroidPolicy = {
  allowSensitiveModules: false,
  playStoreDisclosureConfirmed: false
};

export function validateConfig(input: unknown): asserts input is MindroidConfig {
  if (!input || typeof input !== "object") {
    throw new Error("mindroid.config.json must be an object");
  }

  const value = input as Partial<MindroidConfig>;
  if (!value.name || typeof value.name !== "string") {
    throw new Error("mindroid.config.json: 'name' must be a string");
  }

  if (!Array.isArray(value.plugins) || value.plugins.some((p) => typeof p !== "string")) {
    throw new Error("mindroid.config.json: 'plugins' must be an array of strings");
  }

  if (!Array.isArray(value.capabilities) || value.capabilities.some((c) => typeof c !== "string")) {
    throw new Error("mindroid.config.json: 'capabilities' must be an array of strings");
  }

  if (!value.policy || typeof value.policy !== "object") {
    throw new Error("mindroid.config.json: 'policy' must be an object");
  }

  if (typeof value.policy.allowSensitiveModules !== "boolean") {
    throw new Error("mindroid.config.json: 'policy.allowSensitiveModules' must be boolean");
  }

  if (typeof value.policy.playStoreDisclosureConfirmed !== "boolean") {
    throw new Error("mindroid.config.json: 'policy.playStoreDisclosureConfirmed' must be boolean");
  }
}

export async function loadConfig(configPath: string): Promise<MindroidConfig> {
  if (!existsSync(configPath)) {
    throw new Error("mindroid.config.json not found in current directory");
  }

  const parsed = JSON.parse(await readFile(configPath, "utf8")) as unknown;
  validateConfig(parsed);
  return parsed;
}

export async function saveConfig(configPath: string, config: MindroidConfig): Promise<void> {
  validateConfig(config);
  await writeFile(configPath, JSON.stringify(config, null, 2), "utf8");
}
