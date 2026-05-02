import React, { useEffect, useState } from "react";
import { MindroidRuntime, WebFallbackBridge } from "@mindroid/core";
import { createDeviceInfoPlugin, type DeviceInfo } from "@mindroid/plugin-device-info";

const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });
const devicePlugin = createDeviceInfoPlugin();
runtime.use(devicePlugin);

export function App() {
  const [state, setState] = useState<DeviceInfo | null>(null);

  useEffect(() => {
    devicePlugin
      .api()
      .getInfo()
      .then((value) => setState(value))
      .catch(() => setState(null));
  }, []);

  return <pre>{JSON.stringify(state, null, 2)}</pre>;
}
