import { NativeEventEmitter, NativeModules, Platform } from "react-native";

export type TimeChangeEvent = {
  action: "time_set" | "timezone_changed" | "timezone_offset_changed";
  timeZone?: string;
  utcOffsetMinutes: number;
  previousUtcOffsetMinutes?: number;
  timestamp: number;
};

export type TimeContext = {
  timeZone: string;
  utcOffsetMinutes: number;
  timestamp: number;
};

export type Unsubscribe = () => void;

type NativeAndroidTimeChangeModule = {
  registerTimeChangeHeadlessTask: (taskName: string) => void;
  getCurrentTimeContext: () => Promise<TimeContext>;
  addListener: (eventName: string) => void;
  removeListeners: (count: number) => void;
};

const MODULE_NAME = "RNAndroidTimeChange";
const EVENT_NAME = "RNAndroidTimeChangeEvent";

const nativeModule = NativeModules[MODULE_NAME] as
  | NativeAndroidTimeChangeModule
  | undefined;
const isAndroid = Platform.OS === "android";

const getJsTimeContext = (): TimeContext => {
  const timestamp = Date.now();
  const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";

  return {
    timeZone,
    utcOffsetMinutes: -new Date(timestamp).getTimezoneOffset(),
    timestamp,
  };
};

export const registerTimeChangeListener = (
  listener: (event: TimeChangeEvent) => void,
): Unsubscribe => {
  if (!isAndroid || !nativeModule) {
    return () => undefined;
  }

  const eventEmitter = new NativeEventEmitter(nativeModule);
  const subscription = eventEmitter.addListener(EVENT_NAME, listener);

  return () => subscription.remove();
};

export const registerTimeChangeHeadlessTask = (taskName: string): void => {
  if (!isAndroid || !nativeModule) {
    return;
  }

  nativeModule.registerTimeChangeHeadlessTask(taskName);
};

export const getCurrentTimeContext = async (): Promise<TimeContext> => {
  if (!isAndroid || !nativeModule) {
    return getJsTimeContext();
  }

  return nativeModule.getCurrentTimeContext();
};
