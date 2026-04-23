describe("@niqp/react-native-android-time-change fallback", () => {
  beforeEach(() => {
    jest.resetModules();
  });

  it("uses safe no-op behavior outside Android", async () => {
    jest.doMock("react-native", () => ({
      NativeEventEmitter: jest.fn(),
      NativeModules: {},
      Platform: {
        OS: "ios",
      },
    }));

    const {
      getCurrentTimeContext,
      registerTimeChangeHeadlessTask,
      registerTimeChangeListener,
    } = await import("../index");

    const unsubscribe = registerTimeChangeListener(jest.fn());
    registerTimeChangeHeadlessTask("IgnoredTask");
    const context = await getCurrentTimeContext();

    expect(unsubscribe()).toBeUndefined();
    expect(typeof context.timeZone).toBe("string");
    expect(typeof context.utcOffsetMinutes).toBe("number");
    expect(typeof context.timestamp).toBe("number");
  });
});
