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
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.util.text.SemVer
import io.github.bric3.rectangle.RectangleBundle.message
import io.github.bric3.rectangle.util.Command
import io.github.bric3.rectangle.util.MdlsCommand
import io.github.bric3.rectangle.util.getAppBundleId
import io.github.bric3.rectangle.util.retry
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.hours


@Service(Service.Level.APP)
class RectangleAppService(private val cs: CoroutineScope) {
  private val detectedRectangleVersionFlow = MutableSharedFlow<String?>()

  val detectedVersionFlow: StateFlow<SemVer?> = detectedRectangleVersionFlow
    .map { v ->
      // Rectangle version is not semver, so stick-man approach to append a patch number
      val versionWithPatch = if (v?.count { it == '.' } == 1) {
        "$v.0"
      } else {
        v
      }
      SemVer.parseFromText(versionWithPatch)
    }
    .stateIn(cs, SharingStarted.Eagerly, null)

  val detectedFlow: StateFlow<Boolean> = detectedVersionFlow
    .map { it != null }
    .stateIn(cs, SharingStarted.Eagerly, false)

  init {
    val notified = false
    cs.launch {
      while (true) {
        val version = retry { detectRectangle() }?.let { rectanglePath ->
          retry { detectRectangleVersion(rectanglePath) }
        }
        if (version != null) {
          detectedRectangleVersionFlow.emit(version)
        } else if (!notified) {
          notifyUserOnceIfMissingRectangle()
        }
        delay(1.hours)
      }
    }
  }

  private fun notifyUserOnceIfMissingRectangle() {
    logger.info("Rectangle App not found")
    RectanglePluginApplicationService.getInstance().notifyUser(
      message("rectangle.action.failure.not-found.text"),
      ERROR
    ) notification@{
      addAction(
        @Suppress("DialogTitleCapitalization")
        DumbAwareAction.create(message("rectangle.action.suggested-actions.install-from-web.text")) {
          BrowserUtil.browse("https://rectangleapp.com/")
        }
      )
      BrewRectangleInstaller.brewRectangleInstallAction?.let { delegate ->
        addAction(DumbAwareAction.create(delegate.templateText) {
          ActionUtil.performActionDumbAwareWithCallbacks(
            delegate,
            it,
          )
          this@notification.expire()
        })
      }
    }
  }

  private suspend fun detectRectangle(): Path? {
    val path = if (Files.exists(DEFAULT_INSTALL_LOCATION)) {
      DEFAULT_INSTALL_LOCATION
    } else {
      // find if there's a running Rectangle Process
      // ps aux | grep Rectangle
      val detectedPath = withContext(Dispatchers.IO) {
        object : Command<String?>(
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
        }.run()
      }?.lines()?.firstOrNull {
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
        RectanglePluginApplicationService.getInstance()
          .notifyUser(message("rectangle.action.failure.detect-version.text"), ERROR)
        null
      },
      onProcessFailure = {
        logger.error("Failed to detect Rectangle version: $stderr")
        RectanglePluginApplicationService.getInstance()
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

  fun runRectangleUrlAction(rectangleAction: RectangleWindowAction) {
    runRectangleUrl(ExecuteType.action, rectangleAction.name)
  }

  fun runRectangleUrlTask(rectangleTask: RectangleTask, args: Map<String, String>) {
    runRectangleUrl(ExecuteType.task, rectangleTask.name, args)
  }

  @Suppress("EnumEntryName")
  private enum class ExecuteType { action, task }

  private fun runRectangleUrl(executeType: ExecuteType, name: String, args: Map<String, String> = emptyMap()) {
    cs.launch(CoroutineName("runRectangleUrlAction")) {
      val rectangleUrl = buildString {
        append("rectangle://execute-")
        append(executeType)
        append("?name=")
        append(name)
        args.forEach { k, v ->
          append("&")
          append(k)
          append("=")
          append(v)
        }
      }

      val commandLine = GeneralCommandLine().apply {
        exePath = "/usr/bin/open"
        addParameter("-g")
        addParameter(rectangleUrl)
      }

      val handler = try {
        OSProcessHandler(commandLine)
      } catch (e: Exception) {
        logger.error("Failed to run Rectangle url: $rectangleUrl", e)
        RectanglePluginApplicationService.getInstance()
          .notifyUser(message("rectangle.action.failure.run.text", executeType, name), ERROR)
        return@launch
      }

      val runner = CapturingProcessRunner(handler)
      val output = runner.runProcess(1000)
      if (output.isTimeout || output.exitCode != 0) {
        logger.error("Failed to run Rectangle action $name: ${output.stderr}")
        RectanglePluginApplicationService.getInstance()
          .notifyUser(message("rectangle.action.failure.run.text", executeType, name), ERROR)
      }
    }
  }

  companion object {
    private const val RECTANGLE_BUNDLE_ID = "com.knollsoft.Rectangle"

    private val DEFAULT_INSTALL_LOCATION = Path.of("/Applications/Rectangle.app")

    private val logger = logger<RectangleAppService>()

    fun getInstance() = ApplicationManager.getApplication().service<RectangleAppService>()
  }
}

