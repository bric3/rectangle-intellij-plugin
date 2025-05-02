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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAwareAction
import io.github.bric3.rectangle.RectangleAppService
import io.github.bric3.rectangle.RectangleWindowAction
import io.github.bric3.rectangle.actions.RectangleActionUtil.originalText

abstract class RectangleWindowActionsWithInlineActions(
  actionNames: List<RectangleWindowAction>
) : DumbAwareAction() {

  constructor(category: RectangleWindowAction.Category) : this(category.actionNames.toList())

  init {
    val actionManager = ActionManager.getInstance()

    val actionList = actionNames.mapNotNull {
      actionManager.getAction(RectangleAction.actionId(it))?.apply {
        if (templatePresentation.icon == null) {
          thisLogger().warn("No icon for, action not shown: $it")
          return@mapNotNull null
        }
        templatePresentation.putClientProperty(ActionUtil.ALWAYS_VISIBLE_INLINE_ACTION, true)
      }
    }
    templatePresentation.putClientProperty(ActionUtil.INLINE_ACTIONS, actionList)
  }

  override fun getActionUpdateThread() = BGT

  override fun update(e: AnActionEvent) {
    // padding not necessary since the inline actions are always shown
    e.presentation.text = originalText()
    e.presentation.isEnabled = RectangleAppService.getInstance().detectedFlow.value
  }

  override fun actionPerformed(e: AnActionEvent) {
    // do nothing
  }
}

class RectangleHalvesActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.Halves
)

class RectangleCornersActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.Corners
)

class RectangleThirdsActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.Thirds
)

class RectangleGeneralActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.General
)

class RectangleDisplayActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.Display
)

class RectangleMovesActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.Move
)

class RectangleFourthsActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.Fourths
)

class RectangleSixthsActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowAction.Category.Sixths
)