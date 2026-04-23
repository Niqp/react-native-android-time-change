package com.niqp.reactnativeandroidtimechange

import android.content.Intent
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidManifestTest {
  @Test
  fun declaresReceiverServiceExportedFlagsAndReceiverActions() {
    val manifest = DocumentBuilderFactory.newInstance().apply {
      isNamespaceAware = true
    }
      .newDocumentBuilder()
      .parse(manifestFile())
    val androidNamespace = "http://schemas.android.com/apk/res/android"

    val receivers = manifest.getElementsByTagName("receiver")
    val receiver = (0 until receivers.length)
      .map { receivers.item(it) }
      .first { it.attributes.getNamedItemNS(androidNamespace, "name").nodeValue == ".TimeChangeBroadcastReceiver" }

    assertEquals("true", receiver.attributes.getNamedItemNS(androidNamespace, "enabled").nodeValue)
    assertEquals("false", receiver.attributes.getNamedItemNS(androidNamespace, "exported").nodeValue)

    val services = manifest.getElementsByTagName("service")
    val service = (0 until services.length)
      .map { services.item(it) }
      .first { it.attributes.getNamedItemNS(androidNamespace, "name").nodeValue == ".TimeChangeHeadlessJsTaskService" }

    assertEquals("true", service.attributes.getNamedItemNS(androidNamespace, "enabled").nodeValue)
    assertEquals("false", service.attributes.getNamedItemNS(androidNamespace, "exported").nodeValue)

    val actionNames = receiver.childNodes.asSequence()
      .flatMap { intentFilter ->
        intentFilter.childNodes.asSequence()
      }
      .filter { it.nodeName == "action" }
      .map { it.attributes.getNamedItemNS(androidNamespace, "name").nodeValue }
      .toSet()

    assertTrue(actionNames.contains(Intent.ACTION_TIME_CHANGED))
    assertTrue(actionNames.contains(Intent.ACTION_TIMEZONE_CHANGED))
    assertTrue(actionNames.contains(TimeChangePayloadMapper.ACTION_TIMEZONE_OFFSET_CHANGED))
  }

  private fun manifestFile(): File {
    return listOf(
      File("src/main/AndroidManifest.xml"),
      File("android/src/main/AndroidManifest.xml"),
    ).first { it.exists() }
  }

  private fun org.w3c.dom.NodeList.asSequence(): Sequence<org.w3c.dom.Node> {
    return (0 until length).asSequence().map { item(it) }
  }
}
