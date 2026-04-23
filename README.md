# @niqp/react-native-android-time-change

Android-only React Native bridge for system time-change broadcasts.

The library observes:

- `android.intent.action.TIME_SET`
- `android.intent.action.TIMEZONE_CHANGED`
- `android.intent.action.TIMEZONE_OFFSET_CHANGED` on devices that support it

It normalizes each broadcast and delivers it to JavaScript through a foreground event or an app-owned Headless JS task.

```ts
import {
  getCurrentTimeContext,
  registerTimeChangeHeadlessTask,
  registerTimeChangeListener,
} from "@niqp/react-native-android-time-change";
```

The library does not schedule notifications, store app state, or apply business rules.
