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

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread.EDT
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAwareAction
import icons.RectangleActionsIcons
import io.github.bric3.rectangle.LastIdeFrameScreenOrientationDetection
import io.github.bric3.rectangle.RectangleAppService
import io.github.bric3.rectangle.RectangleWindowAction
import io.github.bric3.rectangle.RectangleBundle.message
import io.github.bric3.rectangle.RectangleBundle.messagePointer
import io.github.bric3.rectangle.isOrientable
import io.github.bric3.rectangle.util.RotatedIcon

/**
 * Open the URL `rectangle://execute-action?name=[rectangleWindowAction]`. Do not activate Rectangle if possible.
 *
 * Available values for `[rectangleWindowAction]`:
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
class RectangleAction(private val rectangleWindowAction: RectangleWindowAction) : DumbAwareAction(
  messagePointer("rectangle.action.text-with-prefix", rectangleWindowAction.toTitleCase()),
  rectangleWindowAction.description(),
  { RectangleActionsIcons.findIcon(rectangleWindowAction.name) }
) {
  val id = actionId(rectangleWindowAction)

  init {
    isEnabledInModalContext = true
  }

  override fun getActionUpdateThread() = EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = true
    e.presentation.text = when (e.place) {
      ActionPlaces.ACTION_SEARCH -> message("rectangle.action.text-with-prefix", rectangleWindowAction.toTitleCase())
      else -> rectangleWindowAction.toTitleCase()
    }

    if (rectangleWindowAction.isOrientable) {
      val angle = if(LastIdeFrameScreenOrientationDetection.isPortrait()) 90.0 else 0.0
      e.presentation.icon = when (val icon = e.presentation.icon) {
        is RotatedIcon -> icon.apply { degrees = angle }
        else -> icon?.let { RotatedIcon(icon, angle) }
      }
    }
  }

  override fun actionPerformed(p0: AnActionEvent) {
    RectangleAppService.getInstance().runRectangleUrlAction(rectangleWindowAction)
  }

  companion object {
    private val logger = logger<RectangleAction>()
    const val ACTION_PREFIX = "rectangle"
    fun actionId(rectangleWindowAction: RectangleWindowAction) = "$ACTION_PREFIX.${rectangleWindowAction.toId()}"
  }
}