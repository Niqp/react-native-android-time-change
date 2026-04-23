package com.niqp.reactnativeandroidtimechange

import android.content.Context
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.CatalystInstance
import com.facebook.react.bridge.JavaScriptContextHolder
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.UIManager
import com.facebook.react.turbomodule.core.interfaces.CallInvokerHolder

class TestReactApplicationContext(context: Context) : ReactApplicationContext(context) {
  data class EmittedEvent(val name: String, val data: Any?)

  var activeReactInstance = true
  val emittedEvents = mutableListOf<EmittedEvent>()

  private val eventEmitter = object : ReactContext.RCTDeviceEventEmitter {
    override fun emit(eventName: String, data: Any?) {
      emittedEvents.add(EmittedEvent(eventName, data))
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T : JavaScriptModule> getJSModule(jsInterface: Class<T>): T {
    if (jsInterface == ReactContext.RCTDeviceEventEmitter::class.java) {
      return eventEmitter as T
    }

    throw IllegalArgumentException("Unsupported JS module: ${jsInterface.name}")
  }

  override fun <T : NativeModule> hasNativeModule(nativeModuleInterface: Class<T>): Boolean = false

  override fun getNativeModules(): Collection<NativeModule> = emptyList()

  override fun <T : NativeModule> getNativeModule(nativeModuleInterface: Class<T>): T? = null

  override fun getNativeModule(moduleName: String): NativeModule? = null

  override fun getCatalystInstance(): CatalystInstance {
    throw UnsupportedOperationException("CatalystInstance is not available in tests.")
  }

  @Deprecated("Deprecated in React Native")
  override fun hasActiveCatalystInstance(): Boolean = activeReactInstance

  override fun hasActiveReactInstance(): Boolean = activeReactInstance

  @Deprecated("Deprecated in React Native")
  override fun hasCatalystInstance(): Boolean = activeReactInstance

  override fun hasReactInstance(): Boolean = activeReactInstance

  override fun destroy() {
    activeReactInstance = false
  }

  override fun handleException(e: Exception) {
    throw e
  }

  @Deprecated("Deprecated in React Native")
  override fun isBridgeless(): Boolean = false

  override fun getJavaScriptContextHolder(): JavaScriptContextHolder? = null

  override fun getJSCallInvokerHolder(): CallInvokerHolder? = null

  override fun getFabricUIManager(): UIManager? = null

  override fun getSourceURL(): String? = null

  override fun registerSegment(segmentId: Int, path: String, callback: Callback) = Unit
}
