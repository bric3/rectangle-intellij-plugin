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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAwareAction
import io.github.bric3.rectangle.RectangleBundle.message
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.hours

@Service(Service.Level.APP)
class RectangleAppService(private val cs: CoroutineScope) {
  val versionFlow : StateFlow<String?>

  val detected: Boolean
    get() = versionFlow.value != null

  init {
    versionFlow = MutableStateFlow(detectRectangleVersion())
    cs.launch {
      while (true) {
        delay(1.hours)
        versionFlow.value = detectRectangleVersion()
      }
    }
  }

  private fun detectRectangleVersion(): String? {
    // Runs
    // mdls -attr kMDItemVersion -raw /Applications/Rectangle.app
    if (!Files.exists(Path.of(INSTALL_LOCATION))) {
      logger.info("Rectangle App not found")
      RectangleApplicationService.getInstance().notifyUser(
        message("rectangle.action.failure.not-found.text"),
        ERROR
      ) {
        addAction(
          @Suppress("DialogTitleCapitalization")
          DumbAwareAction.create(message("rectangle.action.failure.suggested-actions.install-from-web.text")) {
            BrowserUtil.browse("https://rectangleapp.com/")
          },
          // install from brew?
        )
      }
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

  fun <T : DefaultsOp> rectangleDefaults(defaultsOp: T): T {
    val commandLine = GeneralCommandLine().apply {
      exePath = "/usr/bin/defaults"
      defaultsOp.applyParameters(this)
    }

    val handler = try {
      OSProcessHandler(commandLine)
    } catch (e: Exception) {
      logger.error("Failed to run defaults op ${defaultsOp.name}", e)
      return defaultsOp
    }

    val runner = CapturingProcessRunner(handler)
    val output = runner.runProcess(1000)
    if (output.isTimeout || output.exitCode != 0) {
      defaultsOp.handleFailure(output)
    } else {
      defaultsOp.handleSuccess(output)
    }

    return defaultsOp
  }

  fun runRectangleUrlAction(rectangleActionName: String) {
    cs.launch(CoroutineName("runRectangleUrlAction")) {
      val commandLine = GeneralCommandLine().apply {
        exePath = "/usr/bin/open"
        addParameter("-g")
        addParameter("rectangle://execute-action?name=$rectangleActionName")
      }

      val handler = try {
        OSProcessHandler(commandLine)
      } catch (e: Exception) {
        logger.error("Failed to run Rectangle action $rectangleActionName", e)
        RectangleApplicationService.getInstance()
          .notifyUser(message("rectangle.action.failure.run.text", rectangleActionName), ERROR)
        return@launch
      }

      val runner = CapturingProcessRunner(handler)
      val output = runner.runProcess(1000)
      if (output.isTimeout || output.exitCode != 0) {
        logger.error("Failed to run Rectangle action $rectangleActionName: ${output.stderr}")
        RectangleApplicationService.getInstance()
          .notifyUser(message("rectangle.action.failure.run.text", rectangleActionName), ERROR)
      }
    }
  }

  companion object {
    private val INSTALL_LOCATION = "/Applications/Rectangle.app"

    private val logger = logger<RectangleAppService>()

    fun getInstance() = ApplicationManager.getApplication().service<RectangleAppService>()
  }
}

