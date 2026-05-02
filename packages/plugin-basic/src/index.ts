import { defineModule } from "@mindroid/core";

type Invoke = (channel: string, method: string, args?: unknown) => Promise<unknown>;

function createInvoker(ctx: { bridge: { invoke: Invoke } }) {
  return (channel: string, method: string, args?: unknown) => ctx.bridge.invoke(channel, method, args);
}

export const createCameraPlugin = () =>
  defineModule("camera", ["camera.capture"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      takePhoto: () => invoke("camera", "takePhoto"),
      recordVideo: (args?: unknown) => invoke("camera", "recordVideo", args)
    };
  });

export const createLocationPlugin = () =>
  defineModule("location", ["location.gps"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      getCurrentPosition: () => invoke("location", "getCurrentPosition"),
      watchPosition: () => invoke("location", "watchPosition")
    };
  });

export const createSensorPlugin = () =>
  defineModule("sensors", ["sensor.accelerometer", "sensor.gyroscope"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      start: (type: "accelerometer" | "gyroscope") => invoke("sensors", "start", { type }),
      stop: (type: "accelerometer" | "gyroscope") => invoke("sensors", "stop", { type })
    };
  });

export const createNotificationPlugin = () =>
  defineModule("notifications", ["notification.local", "notification.push"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      notify: (payload: unknown) => invoke("notifications", "notify", payload)
    };
  });

export const createContactsPlugin = () =>
  defineModule("contacts", ["contacts.read", "contacts.write"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      list: () => invoke("contacts", "list"),
      create: (contact: unknown) => invoke("contacts", "create", contact)
    };
  });

export const createBluetoothPlugin = () =>
  defineModule("bluetooth", ["bluetooth.scan", "bluetooth.connect"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      scan: () => invoke("bluetooth", "scan"),
      connect: (id: string) => invoke("bluetooth", "connect", { id })
    };
  });

export const createNfcPlugin = () =>
  defineModule("nfc", ["nfc.read", "nfc.write"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      readTag: () => invoke("nfc", "readTag"),
      writeTag: (payload: unknown) => invoke("nfc", "writeTag", payload)
    };
  });

export const createBiometricPlugin = () =>
  defineModule("biometrics", ["biometrics.authenticate"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      authenticate: (message: string) => invoke("biometrics", "authenticate", { message })
    };
  });

export const createBackgroundPlugin = () =>
  defineModule("background", ["background.service"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      startService: (name: string, config?: unknown) => invoke("background", "startService", { name, config }),
      stopService: (name: string) => invoke("background", "stopService", { name })
    };
  });

export const createSmsPlugin = () =>
  defineModule("sms", ["sms.send", "sms.read"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      send: (to: string, message: string) => invoke("sms", "send", { to, message }),
      inbox: () => invoke("sms", "inbox")
    };
  });

export const createMediaPlugin = () =>
  defineModule("media", ["media.audio", "media.video"], (ctx) => {
    const invoke = createInvoker(ctx);
    return {
      play: (uri: string) => invoke("media", "play", { uri }),
      pause: () => invoke("media", "pause"),
      listAudio: () => invoke("media", "listAudio")
    };
  });
