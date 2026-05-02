# @niqp/react-native-android-time-change

Android-only React Native bridge for system time and timezone change broadcasts.

The library observes Android broadcasts for manual time changes, timezone changes,
and timezone offset changes when the device supports them. It normalizes each
broadcast and delivers it to JavaScript through a foreground event listener or an
app-owned Headless JS task.

## Installation

```sh
npm install @niqp/react-native-android-time-change
```

This package supports React Native autolinking on Android. It has no iOS native
implementation.

## Usage

### Listen while the app is running

```ts
import { registerTimeChangeListener } from "@niqp/react-native-android-time-change";

const unsubscribe = registerTimeChangeListener((event) => {
  console.log(event.action, event.timeZone, event.utcOffsetMinutes);
});

// Later, when the listener is no longer needed:
unsubscribe();
```

### Register a Headless JS task

Register the Headless JS task with React Native, then pass the same task name to
the native module. Android can then deliver time-change payloads when the app is
not in the foreground.

```ts
import { AppRegistry } from "react-native";
import { registerTimeChangeHeadlessTask } from "@niqp/react-native-android-time-change";

const TASK_NAME = "TimeChangeTask";

AppRegistry.registerHeadlessTask(TASK_NAME, () => async (event) => {
  console.log(event.action, event.utcOffsetMinutes);
});

registerTimeChangeHeadlessTask(TASK_NAME);
```

### Read the current time context

```ts
import { getCurrentTimeContext } from "@niqp/react-native-android-time-change";

const context = await getCurrentTimeContext();
console.log(context.timeZone, context.utcOffsetMinutes, context.timestamp);
```

On Android, `getCurrentTimeContext()` resolves from the native module. On other
platforms, it falls back to JavaScript date and timezone APIs.

## API

```ts
type TimeChangeEvent = {
  action: "time_set" | "timezone_changed" | "timezone_offset_changed";
  timeZone?: string;
  utcOffsetMinutes: number;
  previousUtcOffsetMinutes?: number;
  timestamp: number;
};

type TimeContext = {
  timeZone: string;
  utcOffsetMinutes: number;
  timestamp: number;
};

type Unsubscribe = () => void;
```

### `registerTimeChangeListener(listener)`

Registers a foreground listener for normalized time-change events and returns an
unsubscribe function.

On non-Android platforms, or if the native module is unavailable, this is a no-op
and returns a no-op unsubscribe function.

### `registerTimeChangeHeadlessTask(taskName)`

Stores the app-owned Headless JS task name that should receive normalized
time-change events.

On non-Android platforms, or if the native module is unavailable, this is a no-op.

### `getCurrentTimeContext()`

Returns the current timezone, UTC offset in minutes, and timestamp in
milliseconds.

## Android Broadcasts

The native Android module listens for:

- `android.intent.action.TIME_SET`
- `android.intent.action.TIMEZONE_CHANGED`
- `android.intent.action.TIMEZONE_OFFSET_CHANGED`

`TIMEZONE_OFFSET_CHANGED` is available only on Android versions and devices that
emit that broadcast.

## Scope

This package does not schedule notifications, store app state, apply business
rules, or provide an iOS implementation. It only bridges Android system
time-change signals into React Native JavaScript.

## License

MIT
