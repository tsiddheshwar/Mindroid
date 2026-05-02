/**
 * JS-side native widget types for the Mindroid NativeRenderer.
 *
 * Build a widget tree and pass it to the renderer channel:
 *
 *   bridge.invoke("renderer", "render", { widgets: [...] })
 */

export type WidgetType = "View" | "Text" | "Button" | "ScrollView" | "Column" | "Row";

export interface BaseWidget {
  type: WidgetType;
  key?: string;
  children?: Widget[];
}

export interface TextView extends BaseWidget {
  type: "Text";
  text: string;
  size?: number;
}

export interface ButtonWidget extends BaseWidget {
  type: "Button";
  text: string;
  /** Handler id string registered via registerEventHandler() */
  onPress?: string;
}

export interface ViewWidget extends BaseWidget {
  type: "View";
}

export interface ScrollViewWidget extends BaseWidget {
  type: "ScrollView";
}

export interface ColumnWidget extends BaseWidget {
  type: "Column";
}

export interface RowWidget extends BaseWidget {
  type: "Row";
}

export type Widget = TextView | ButtonWidget | ViewWidget | ScrollViewWidget | ColumnWidget | RowWidget;

export interface RenderSpec {
  widgets: Widget[];
}

/**
 * Serializes a widget tree to the JSON format expected by NativeRenderer.kt.
 */
export function serializeWidgetTree(widgets: Widget[]): string {
  return JSON.stringify(widgets);
}
