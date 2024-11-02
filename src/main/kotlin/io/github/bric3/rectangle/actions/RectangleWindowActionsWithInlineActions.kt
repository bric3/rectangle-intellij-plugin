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
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import io.github.bric3.rectangle.RectangleAppService
import io.github.bric3.rectangle.RectangleWindowActionName
import io.github.bric3.rectangle.actions.RectangleActionUtil.originalText
import kotlin.reflect.full.declaredMemberProperties

abstract class RectangleWindowActionsWithInlineActions(
  private val actionNames: List<RectangleWindowActionName>
) : DumbAwareAction() {

  constructor(category: RectangleWindowActionName.Category) : this(category.actionNames.toList())

  init {
    val actionManager = ActionManager.getInstance()

    val actionList = actionNames.mapNotNull {
      actionManager.getAction(RectangleAction.actionId(it))
    }
    templatePresentation.putClientProperty(ActionUtil.INLINE_ACTIONS, actionList)
  }

  override fun getActionUpdateThread() = BGT

  override fun update(e: AnActionEvent) {
    // padding to give space to inline actions
    val charWidth = IdeFocusManager.getGlobalInstance().lastFocusedFrame
      ?.component
      ?.getFontMetrics(JBFont.label())
      ?.widths
      ?.average()
      ?.toInt()

    val iconBorders = 3
    e.presentation.text = originalText()?.padEnd(actionNames.size * JBUI.scale((charWidth ?: 6) + iconBorders))
    e.presentation.isEnabled = RectangleAppService.getInstance().detected

    // 243 replace with the key from ActionUtil
    ActionUtil::class.declaredMemberProperties
      .firstOrNull { it.name == "ALWAYS_VISIBLE_INLINE_ACTION"}
      ?.call()
      ?.let {
        @Suppress("UNCHECKED_CAST")
        it as? Key<Boolean>
      }?.let { key ->
        e.presentation.putClientProperty(key, true)
      }

  }

  override fun actionPerformed(e: AnActionEvent) {
    // do nothing
  }
}

class RectangleHalvesActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.Halves
)

class RectangleCornersActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.Corners
)

class RectangleThirdsActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.Thirds
)

class RectangleGeneralActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.General
)

class RectangleDisplayActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.Display
)

class RectangleMovesActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.Move
)

class RectangleFourthsActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.Fourths
)

class RectangleSixthsActions : RectangleWindowActionsWithInlineActions(
  RectangleWindowActionName.Category.Sixths
)