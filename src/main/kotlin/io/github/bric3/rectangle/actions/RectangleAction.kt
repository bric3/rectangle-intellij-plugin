/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.rectangle.actions

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.execution.process.OSProcessHandler
import com.intellij.notification.NotificationType.ERROR
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAwareAction
import io.github.bric3.rectangle.RectangleActionName
import io.github.bric3.rectangle.RectangleApplicationService
import io.github.bric3.rectangle.RectangleBundle.message
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch

/**
 * Open the URL `rectangle://execute-action?name=[rectangleActionName]`. Do not activate Rectangle if possible.
 *
 * Available values for `[rectangleActionName]`:
 *   * `left-half`, `right-half`, `center-half`, `top-half`, `bottom-half`,
 *   * `top-left`, `top-right`, `bottom-left`, `bottom-right`,
 *   * `first-third`, `center-third`, `last-third`,
 *   * `first-two-thirds`, `last-two-thirds`,
 *   * `maximize`, `almost-maximize`, `maximize-height`, `smaller`, `larger`, `center`, `center-prominently`, `restore`,
 *   * `next-display`, `previous-display`,
 *   * `move-left`, `move-right`, `move-up`, `move-down`,
 *   * `first-fourth`, `second-fourth`, `third-fourth`, `last-fourth`, `first-three-fourths`, `last-three-fourths`,
 *   * `top-left-sixth`, `top-center-sixth`, `top-right-sixth`, `bottom-left-sixth`, `bottom-center-sixth`, `bottom-right-sixth`,
 *   * `specified`, `reverse-all`,
 *   * `top-left-ninth`, `top-center-ninth`, `top-right-ninth`, `middle-left-ninth`, `middle-center-ninth`, `middle-right-ninth`, `bottom-left-ninth`, `bottom-center-ninth`, `bottom-right-ninth`,
 *   * `top-left-third`, `top-right-third`, `bottom-left-third`, `bottom-right-third`,
 *   * `top-left-eighth`, `top-center-left-eighth`, `top-center-right-eighth`, `top-right-eighth`, `bottom-left-eighth`, `bottom-center-left-eighth`, `bottom-center-right-eighth`, `bottom-right-eighth`,
 *   * `tile-all`, `cascade-all`, `cascade-active-app`
 */
class RectangleAction(private val rectangleActionName: RectangleActionName) : DumbAwareAction(
  message("rectangle.action.text-with-prefix", rectangleActionName.toTitleCase())
) {
  val id = "$ACTION_PREFIX.${rectangleActionName.toId()}"

  init {
    isEnabledInModalContext = true
  }

  override fun getActionUpdateThread() = BGT

  override fun update(e: AnActionEvent) {
    if (e.place == ActionPlaces.ACTION_SEARCH) {
      e.presentation.text = message("rectangle.action.text-with-prefix", rectangleActionName.toTitleCase())
    }
    e.presentation.text = rectangleActionName.toTitleCase()
    e.presentation.description = rectangleActionName.description()
    e.presentation.isEnabledAndVisible = true
  }

  override fun actionPerformed(p0: AnActionEvent) {
    RectangleApplicationService.getInstance()
      .newChildScope(CoroutineName("Running Rectangle action $rectangleActionName")).launch {

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
    private val logger = logger<RectangleAction>()
    const val ACTION_PREFIX = "rectangle"
  }
}