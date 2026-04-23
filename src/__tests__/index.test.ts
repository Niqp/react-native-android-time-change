const addListenerMock = jest.fn();
const removeMock = jest.fn();
const registerTaskMock = jest.fn();
const getCurrentTimeContextMock = jest.fn();

jest.mock("react-native", () => ({
  NativeEventEmitter: jest.fn().mockImplementation(() => ({
    addListener: addListenerMock,
  })),
  NativeModules: {
    RNAndroidTimeChange: {
      addListener: jest.fn(),
      removeListeners: jest.fn(),
      registerTimeChangeHeadlessTask: registerTaskMock,
      getCurrentTimeContext: getCurrentTimeContextMock,
    },
  },
  Platform: {
    OS: "android",
  },
}));

describe("@niqp/react-native-android-time-change", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    addListenerMock.mockReturnValue({ remove: removeMock });
    getCurrentTimeContextMock.mockResolvedValue({
      timeZone: "Europe/Madrid",
      utcOffsetMinutes: 60,
      timestamp: 1_700_000_000_000,
    });
  });

  it("registers and removes foreground listeners", async () => {
    const { registerTimeChangeListener } = await import("../index");
    const listener = jest.fn();

    const unsubscribe = registerTimeChangeListener(listener);

    expect(addListenerMock).toHaveBeenCalledWith(
      "RNAndroidTimeChangeEvent",
      listener,
    );

    unsubscribe();

    expect(removeMock).toHaveBeenCalledTimes(1);
  });

  it("forwards the headless task name to native", async () => {
    const { registerTimeChangeHeadlessTask } = await import("../index");

    registerTimeChangeHeadlessTask("YAHTTimeChangeTask");

    expect(registerTaskMock).toHaveBeenCalledWith("YAHTTimeChangeTask");
  });

  it("reads current time context from native on Android", async () => {
    const { getCurrentTimeContext } = await import("../index");

    await expect(getCurrentTimeContext()).resolves.toEqual({
      timeZone: "Europe/Madrid",
      utcOffsetMinutes: 60,
      timestamp: 1_700_000_000_000,
    });
  });
});
