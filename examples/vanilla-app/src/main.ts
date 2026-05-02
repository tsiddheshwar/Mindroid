import { MindroidRuntime, WebFallbackBridge } from "@mindroid/core";
import { createDeviceInfoPlugin } from "@mindroid/plugin-device-info";
import { createCameraPlugin } from "@mindroid/plugin-basic";

const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });

const devicePlugin = createDeviceInfoPlugin();
const cameraPlugin = createCameraPlugin();
runtime.use(devicePlugin).use(cameraPlugin);

console.log("Installed modules:", runtime.getModuleNames());
console.log("Capabilities:", runtime.getCapabilities());
