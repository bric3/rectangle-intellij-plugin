/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.rectangle.util

import io.github.bric3.rectangle.RectanglePlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Try gets the App Bundle Id, first via spotlight, then by trying to read the `Info.plist` file.
 */
suspend fun getAppBundleId(appPath: Path) =
  bundleIdViaSpotlightCommand(appPath).takeIf { it != "(null)" } ?: getAppBundleIdViaInfoPlist(appPath)

/**
 * Gets the App Bundle Id, using Spotlight cli tool `mdls`.
 *
 * Runs `mdls -attr kMDItemCFBundleIdentifier -raw ....app`
 */
private suspend fun bundleIdViaSpotlightCommand(appPath: Path) = withContext(Dispatchers.IO) {
  MdlsCommand(
    appPath = appPath.toString(),
    attributeName = "kMDItemCFBundleIdentifier",
    onProcessExecutionException = {
      RectanglePlugin.logger.error("Failed to get bundle id for $appPath", it)
      null
    },
    onProcessFailure = {
      RectanglePlugin.logger.error("Failed to get bundle id for $appPath: $stderr")
      null
    },
    onProcessSuccess = { stdout.trim() }
  ).run()
}

private suspend fun getAppBundleIdViaInfoPlist(appPath: Path) = withContext(Dispatchers.IO) {
  val path = (appPath.resolve("Info.plist").takeIf { Files.exists(it) }
    ?: appPath.resolve("Contents").resolve("Info.plist").takeIf { Files.exists(it) })
    ?: return@withContext null

  try {
    // Now read the CFBundleIdentifier key
    val dbf = DocumentBuilderFactory.newDefaultInstance()
    dbf.setFeature("http://xml.org/sax/features/validation", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    val document = dbf.newDocumentBuilder().parse(Files.newInputStream(path))

    val xpath = XPathFactory.newInstance().newXPath()
    val bundleId = xpath.evaluate(
      "//string[preceding-sibling::key = \"CFBundleIdentifier\"][1]",
      document,
      XPathConstants.STRING
    ) as? String

    bundleId?.takeIf { it.isNotBlank() }
  } catch (e: Exception) {
    null
  }
}