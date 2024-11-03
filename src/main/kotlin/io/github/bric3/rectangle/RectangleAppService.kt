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
import com.intellij.execution.process.ProcessOutput
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType.ERROR
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAwareAction
import io.github.bric3.rectangle.RectangleBundle.message
import io.github.bric3.rectangle.util.Command
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
  val rectanglePathFlow : StateFlow<Path?>
  val versionFlow : StateFlow<String?>

  val detected: Boolean
    get() = versionFlow.value != null

  init {
    rectanglePathFlow = MutableStateFlow(detectRectangle())
    versionFlow = MutableStateFlow(detectRectangleVersion(rectanglePathFlow.value))
    cs.launch {
      while (true) {
        delay(1.hours)
        rectanglePathFlow.value = detectRectangle()
        versionFlow.value = detectRectangleVersion(rectanglePathFlow.value)
      }
    }

    notifyUserOnceIfMissingRectangle()
  }

  private fun notifyUserOnceIfMissingRectangle() {
    if (rectanglePathFlow.value == null) {
      logger.info("Rectangle App not found")
      RectangleApplicationService.getInstance().notifyUser(
        message("rectangle.action.failure.not-found.text"),
        ERROR
      ) {
        addAction(
          @Suppress("DialogTitleCapitalization")
          DumbAwareAction.create(message("rectangle.action.suggested-actions.install-from-web.text")) {
            BrowserUtil.browse("https://rectangleapp.com/")
          }
        )
        BrewRectangleInstaller.brewRectangleInstallAction?.let { addAction(it) }
      }
    }
  }

  private fun detectRectangle(): Path? {
    val path = if (Files.exists(DEFAULT_INSTALL_LOCATION)) {
      DEFAULT_INSTALL_LOCATION
    } else {
      // find if there's a running Rectangle Process
      // ps aux | grep Rectangle
      val detectedPath = object : Command<String?>(
        onProcessExecutionException = {
          logger.error("Failed to run ps command", it)
          null
        },
        onProcessFailure = {
          logger.error("Failed to run ps command: $stderr")
          null
        },
        onProcessSuccess = { stdout.trim() }
      ) {
        override fun GeneralCommandLine.commandLine() {
          exePath = "/bin/ps"
          addParameters("-e", "-o", "command")
        }
      }.run()?.lines()?.firstOrNull {
        it.contains("Rectangle.app")
      }?.let {
        it.substringBefore("Rectangle.app", "") + "Rectangle.app"
      }?.let {
        Path.of(it)
      }

      detectedPath
    }

    return path?.takeIf { getAppBundleId(it) == RECTANGLE_BUNDLE_ID }
  }

  private fun detectRectangleVersion(appPath: Path?): String? {
    appPath ?: return null

    // Runs
    // mdls -attr kMDItemVersion -raw /Applications/Rectangle.app
    return MdlsCommand(
      appPath = appPath.toString(),
      attributeName = "kMDItemVersion",
      onProcessExecutionException = {
        logger.error("Failed to detect Rectangle version", it)
        RectangleApplicationService.getInstance()
          .notifyUser(message("rectangle.action.failure.detect-version.text"), ERROR)
        null
      },
      onProcessFailure = {
          logger.error("Failed to detect Rectangle version: $stderr")
          RectangleApplicationService.getInstance()
            .notifyUser(message("rectangle.action.failure.detect-version.text"), ERROR)
        null
     },
      onProcessSuccess = { stdout.trim() }
    ).run()
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

  // Runs
  // mdls -attr kMDItemCFBundleIdentifier -raw ....app
  fun getAppBundleId(path: Path) = MdlsCommand(
    appPath = path.toString(),
    attributeName = "kMDItemCFBundleIdentifier",
    onProcessExecutionException = {
      logger.error("Failed to get bundle id for $path", it)
      null
    },
    onProcessFailure = {
      logger.error("Failed to get bundle id for $path: $stderr")
      null
    },
    onProcessSuccess = { stdout.trim() }
  ).run()

  class MdlsCommand<T>(
    private val appPath: String,
    private val attributeName: String,
    onProcessExecutionException: (Exception) -> T,
    onProcessFailure: ProcessOutput.() -> T,
    onProcessSuccess: ProcessOutput.() -> T,
  ) : Command<T>(
    onProcessExecutionException,
    onProcessFailure,
    onProcessSuccess
  ) {
    override fun GeneralCommandLine.commandLine() {
      exePath = "/usr/bin/mdls"
      addParameters("-attr", attributeName)
      addParameter("-raw")
      addParameter(appPath)
    }
  }

  companion object {
    private const val RECTANGLE_BUNDLE_ID = "com.knollsoft.Rectangle"

    private val DEFAULT_INSTALL_LOCATION = Path.of("/Applications/Rectangle.app")

    private val logger = logger<RectangleAppService>()

    fun getInstance() = ApplicationManager.getApplication().service<RectangleAppService>()
  }
}

