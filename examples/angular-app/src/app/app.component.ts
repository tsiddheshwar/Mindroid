import { Component } from "@angular/core";
import { MindroidRuntime, WebFallbackBridge } from "@mindroid/core";
import { createNetworkPlugin } from "@mindroid/plugin-network";

const runtime = new MindroidRuntime({ bridge: new WebFallbackBridge() });
const network = createNetworkPlugin();
runtime.use(network);

@Component({
  selector: "app-root",
  standalone: true,
  template: `<h1>Mindroid Angular Starter</h1><p>Modules: {{ modules }}</p>`
})
export class AppComponent {
  modules = runtime.getModuleNames().join(", ");
}
