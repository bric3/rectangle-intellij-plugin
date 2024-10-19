/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.rectangle

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.execution.process.OSProcessHandler
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType.ERROR
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAwareAction
import io.github.bric3.rectangle.RectangleBundle.message
import java.nio.file.Files
import java.nio.file.Path


object RectangleDetector {
  private const val INSTALL_LOCATION = "/Applications/Rectangle.app"
  private val logger = thisLogger()

  fun detectRectangleVersion(): String? {
    // Runs
    // mdls -attr kMDItemVersion -raw /Applications/Rectangle.app
    if (!Files.exists(Path.of(INSTALL_LOCATION))) {
      logger.info("Rectangle App not found")
      RectangleApplicationService.getInstance().notifyUser(
        message("rectangle.action.failure.not-found.text"),
        ERROR,
        @Suppress("DialogTitleCapitalization")
        DumbAwareAction.create(message("rectangle.action.failure.suggested-actions.install-from-web.text")) {
          BrowserUtil.browse("https://rectangleapp.com/")
        },
        // install from brew?
      )
      return null
    }

    val commandLine = GeneralCommandLine().apply {
      exePath = "/usr/bin/mdls"
      addParameters("-attr", "kMDItemVersion")
      addParameter("-raw")
      addParameter(INSTALL_LOCATION)
    }

    val handler = try {
      OSProcessHandler(commandLine)
    } catch (e: Exception) {
      logger.error("Failed to detect Rectangle version", e)
      RectangleApplicationService.getInstance()
        .notifyUser(message("rectangle.action.failure.detect-version.text"), ERROR)
      return null
    }

    val runner = CapturingProcessRunner(handler)
    val output = runner.runProcess(1000)
    if (output.isTimeout || output.exitCode != 0) {
      logger.error("Failed to detect Rectangle version: ${output.stderr}")
      RectangleApplicationService.getInstance()
        .notifyUser(message("rectangle.action.failure.detect-version.text"), ERROR)
    }

    return output.stdout.trim()
  }
}