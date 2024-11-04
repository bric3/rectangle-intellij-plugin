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
import com.intellij.ide.impl.ProjectUtil
import com.intellij.notification.NotificationType.ERROR
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.platform.ide.progress.withBackgroundProgress
import io.github.bric3.rectangle.RectangleBundle.message
import io.github.bric3.rectangle.util.Command
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path


object BrewRectangleInstaller {
  private val logger = thisLogger()
  private const val BREW_BIN_PATH = "/opt/homebrew/bin/brew"

  val brewRectangleInstallAction: DumbAwareAction?
    get() = if (Files.isExecutable(Path.of(BREW_BIN_PATH))) {
      DumbAwareAction.create(message("rectangle.action.suggested-actions.brew-install.text")) {
        RectangleApplicationService.getInstance().newChildScope(CoroutineName("brew-install-rectangle")).launch {
          withProgressReporterIfPossible {
            withContext(Dispatchers.IO) {
              coroutineToIndicator {
                val indicator = ProgressManager.getInstance().progressIndicator
                brewInstallRectangle(indicator)
              }
            }
          }
        }
      }
    } else {
      null
    }

  private suspend fun CoroutineScope.withProgressReporterIfPossible(block: suspend CoroutineScope.() -> Unit) {
    val activeProject = ProjectUtil.getActiveProject()
    if (activeProject != null) {
      withBackgroundProgress(
        activeProject,
        message("rectangle.action.suggested-actions.brew-install.progress.text")
      ) {
        block()
      }
    } else {
      block()
    }
  }

  private fun brewInstallRectangle(progressIndicator: ProgressIndicator) {
    object : Command<Unit>(
      onProcessExecutionException = {
        logger.error("Error while installing Rectangle", it)
        RectangleApplicationService.getInstance()
          .notifyUser(message("rectangle.action.suggested-actions.brew-install.failure.text"), ERROR)
      },
      onProcessFailure = {
        logger.error("Error while installing Rectangle (timeout: $isTimeout): $stderr")
        RectangleApplicationService.getInstance()
          .notifyUser(message("rectangle.action.suggested-actions.brew-install.failure.text"), ERROR)
      },
      onProcessSuccess = { }
    ) {
      override val progressIndicator = progressIndicator

      override fun GeneralCommandLine.commandLine() {
        exePath = BREW_BIN_PATH
        addParameter("install")
        addParameter("rectangle")
      }
    }.run()
  }
}