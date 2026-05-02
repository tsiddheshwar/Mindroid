#!/usr/bin/env node
import { cp, mkdir, writeFile } from "node:fs/promises";
import { existsSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";
import { buildAndroidIcons } from "@mindroid/icon-builder";
import { DEFAULT_POLICY, loadConfig, saveConfig, type MindroidConfig } from "./config.js";
import { discoverPlugin } from "./plugin-registry.js";

const thisDir = dirname(fileURLToPath(import.meta.url));

function printHelp() {
  console.log(`Mindroid CLI\n\nCommands:\n  create <app-name>\n  plugin add <name>\n  plugin list\n  run\n  build [debug|release|aab]\n  icon <input.png> [icon-name]\n`);
}

function collectCapabilities(plugins: string[]): string[] {
  const capabilities = new Set<string>();
  for (const plugin of plugins) {
    const metadata = discoverPlugin(plugin);
    for (const capability of metadata?.capabilities ?? []) {
      capabilities.add(capability);
    }
  }
  return [...capabilities].sort();
}

function hasSensitivePlugins(plugins: string[]): boolean {
  return plugins.some((plugin) => discoverPlugin(plugin)?.sensitive);
}

function validateReleasePolicy(config: MindroidConfig) {
  if (!hasSensitivePlugins(config.plugins)) {
    return;
  }

  if (!config.policy.allowSensitiveModules) {
    throw new Error(
      "Sensitive modules are installed. Set policy.allowSensitiveModules=true in mindroid.config.json to build release artifacts."
    );
  }

  if (!config.policy.playStoreDisclosureConfirmed) {
    throw new Error(
      "Sensitive modules require Play policy disclosures. Set policy.playStoreDisclosureConfirmed=true in mindroid.config.json after legal review."
    );
  }
}

async function runGradle(args: string[], cwd: string) {
  const gradlew = process.platform === "win32" ? "gradlew.bat" : "./gradlew";
  const command = existsSync(join(cwd, gradlew))
    ? gradlew
    : process.platform === "win32"
      ? "gradle.bat"
      : "gradle";

  await new Promise<void>((resolvePromise, rejectPromise) => {
    const child = spawn(command, args, { cwd, stdio: "inherit", shell: process.platform === "win32" });
    child.on("close", (code) => {
      if (code === 0) {
        resolvePromise();
        return;
      }
      rejectPromise(new Error(`Gradle exited with code ${code ?? -1}`));
    });
  });
}

async function commandCreate(appName: string) {
  const targetDir = resolve(process.cwd(), appName);
  const templateDir = resolve(thisDir, "../../../templates/android-host");

  if (existsSync(targetDir)) {
    throw new Error(`Target directory already exists: ${targetDir}`);
  }

  await mkdir(dirname(targetDir), { recursive: true });
  await cp(templateDir, targetDir, { recursive: true });

  const plugins = ["@mindroid/plugin-device-info", "@mindroid/plugin-network"];
  const config: MindroidConfig = {
    name: appName,
    plugins,
    capabilities: collectCapabilities(plugins),
    policy: { ...DEFAULT_POLICY }
  };

  await saveConfig(join(targetDir, "mindroid.config.json"), config);

  await writeFile(
    join(targetDir, "keystore.properties.example"),
    [
      "storeFile=release.keystore",
      "storePassword=replace-me",
      "keyAlias=mindroid",
      "keyPassword=replace-me"
    ].join("\n"),
    "utf8"
  );

  console.log(`Created Mindroid app at ${targetDir}`);
}

async function commandPluginAdd(name: string) {
  const configPath = resolve(process.cwd(), "mindroid.config.json");
  const config = await loadConfig(configPath);

  const plugins = new Set(config.plugins);
  plugins.add(name);
  config.plugins = [...plugins].sort();
  config.capabilities = collectCapabilities(config.plugins);

  await saveConfig(configPath, config);

  const discovered = discoverPlugin(name);
  if (!discovered) {
    console.log(`Plugin added: ${name} (no capability metadata found)`);
    return;
  }

  console.log(`Plugin added: ${name}`);
  console.log(`Capabilities discovered: ${discovered.capabilities.join(", ") || "none"}`);
  if (discovered.sensitive) {
    console.log("This plugin is marked sensitive. Review policy fields in mindroid.config.json before release builds.");
  }
}

async function commandPluginList() {
  const configPath = resolve(process.cwd(), "mindroid.config.json");
  const config = await loadConfig(configPath);

  if (config.plugins.length === 0) {
    console.log("No plugins installed.");
    return;
  }

  console.log("Installed plugins:");
  for (const plugin of config.plugins) {
    const metadata = discoverPlugin(plugin);
    console.log(`- ${plugin}${metadata?.sensitive ? " (sensitive)" : ""}`);
  }
}

async function commandBuild(modeArg: string | undefined) {
  const mode = modeArg === "release" || modeArg === "aab" ? modeArg : "debug";
  const config = await loadConfig(resolve(process.cwd(), "mindroid.config.json"));

  if (mode !== "debug") {
    validateReleasePolicy(config);
  }

  if (mode === "aab") {
    await runGradle(["bundleRelease"], process.cwd());
    return;
  }

  if (mode === "release") {
    await runGradle(["assembleRelease", "bundleRelease"], process.cwd());
    return;
  }

  await runGradle(["assembleDebug"], process.cwd());
}

async function commandIcon(inputPng: string, iconName = "ic_launcher") {
  const outputResDir = resolve(process.cwd(), "app/src/main/res");
  await buildAndroidIcons({
    inputPngPath: resolve(process.cwd(), inputPng),
    outputResDir,
    iconName,
    generateVector: true
  });
  console.log(`Generated Android icon assets in ${outputResDir}`);
}

async function main() {
  const [command, subcommand, ...rest] = process.argv.slice(2);

  if (!command) {
    printHelp();
    return;
  }

  if (command === "create") {
    const appName = subcommand;
    if (!appName) {
      throw new Error("App name is required");
    }
    await commandCreate(appName);
    return;
  }

  if (command === "plugin" && subcommand === "add") {
    const pluginName = rest[0];
    if (!pluginName) {
      throw new Error("Plugin name is required");
    }
    await commandPluginAdd(pluginName);
    return;
  }

  if (command === "plugin" && subcommand === "list") {
    await commandPluginList();
    return;
  }

  if (command === "run") {
    await runGradle(["installDebug"], process.cwd());
    return;
  }

  if (command === "build") {
    await commandBuild(subcommand);
    return;
  }

  if (command === "icon") {
    const inputPng = subcommand;
    if (!inputPng) {
      throw new Error("Input PNG path is required");
    }
    await commandIcon(inputPng, rest[0]);
    return;
  }

  printHelp();
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : error);
  process.exitCode = 1;
});
