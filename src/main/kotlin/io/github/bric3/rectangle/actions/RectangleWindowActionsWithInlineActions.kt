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
import io.github.bric3.rectangle.RectangleAppService
import io.github.bric3.rectangle.RectangleWindowActionName
import io.github.bric3.rectangle.RectangleWindowActionName.`almost-maximize`
import io.github.bric3.rectangle.RectangleWindowActionName.`bottom-center-sixth`
import io.github.bric3.rectangle.RectangleWindowActionName.`bottom-half`
import io.github.bric3.rectangle.RectangleWindowActionName.`bottom-left`
import io.github.bric3.rectangle.RectangleWindowActionName.`bottom-left-sixth`
import io.github.bric3.rectangle.RectangleWindowActionName.`bottom-right`
import io.github.bric3.rectangle.RectangleWindowActionName.`bottom-right-sixth`
import io.github.bric3.rectangle.RectangleWindowActionName.center
import io.github.bric3.rectangle.RectangleWindowActionName.`center-half`
import io.github.bric3.rectangle.RectangleWindowActionName.`center-third`
import io.github.bric3.rectangle.RectangleWindowActionName.`first-fourth`
import io.github.bric3.rectangle.RectangleWindowActionName.`first-third`
import io.github.bric3.rectangle.RectangleWindowActionName.`first-three-fourths`
import io.github.bric3.rectangle.RectangleWindowActionName.`first-two-thirds`
import io.github.bric3.rectangle.RectangleWindowActionName.larger
import io.github.bric3.rectangle.RectangleWindowActionName.`last-fourth`
import io.github.bric3.rectangle.RectangleWindowActionName.`last-third`
import io.github.bric3.rectangle.RectangleWindowActionName.`last-three-fourths`
import io.github.bric3.rectangle.RectangleWindowActionName.`last-two-thirds`
import io.github.bric3.rectangle.RectangleWindowActionName.`left-half`
import io.github.bric3.rectangle.RectangleWindowActionName.maximize
import io.github.bric3.rectangle.RectangleWindowActionName.`maximize-height`
import io.github.bric3.rectangle.RectangleWindowActionName.`move-down`
import io.github.bric3.rectangle.RectangleWindowActionName.`move-left`
import io.github.bric3.rectangle.RectangleWindowActionName.`move-right`
import io.github.bric3.rectangle.RectangleWindowActionName.`move-up`
import io.github.bric3.rectangle.RectangleWindowActionName.`next-display`
import io.github.bric3.rectangle.RectangleWindowActionName.`previous-display`
import io.github.bric3.rectangle.RectangleWindowActionName.restore
import io.github.bric3.rectangle.RectangleWindowActionName.`right-half`
import io.github.bric3.rectangle.RectangleWindowActionName.`second-fourth`
import io.github.bric3.rectangle.RectangleWindowActionName.smaller
import io.github.bric3.rectangle.RectangleWindowActionName.`third-fourth`
import io.github.bric3.rectangle.RectangleWindowActionName.`top-center-sixth`
import io.github.bric3.rectangle.RectangleWindowActionName.`top-half`
import io.github.bric3.rectangle.RectangleWindowActionName.`top-left`
import io.github.bric3.rectangle.RectangleWindowActionName.`top-left-sixth`
import io.github.bric3.rectangle.RectangleWindowActionName.`top-right`
import io.github.bric3.rectangle.RectangleWindowActionName.`top-right-sixth`
import io.github.bric3.rectangle.actions.RectangleActionUtil.originalText
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties

abstract class RectangleWindowActionsWithInlineActions(private val actionNames: List<RectangleWindowActionName>)
  : DumbAwareAction() {

  constructor(vararg actionNames: RectangleWindowActionName) : this(actionNames.toList())

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
    e.presentation.text = originalText()?.padEnd(50)
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
  `left-half`, `right-half`, `center-half`, `top-half`, `bottom-half`
)

class RectangleCornersActions : RectangleWindowActionsWithInlineActions(
  `top-left`, `top-right`, `bottom-left`, `bottom-right`
)

class RectangleThirdsActions : RectangleWindowActionsWithInlineActions(
  `first-third`, `center-third`, `last-third`,
  `first-two-thirds`, `last-two-thirds`,
)

class RectangleGeneralActions : RectangleWindowActionsWithInlineActions(
  maximize, `almost-maximize`, `maximize-height`, smaller, larger, center, restore
)

class RectangleDisplayActions : RectangleWindowActionsWithInlineActions(
  `previous-display`, `next-display`
)

class RectangleMovesActions : RectangleWindowActionsWithInlineActions(
  `move-left`, `move-right`, `move-up`, `move-down`
)

class RectangleFourthsActions : RectangleWindowActionsWithInlineActions(
  `first-fourth`, `second-fourth`, `third-fourth`, `last-fourth`, `first-three-fourths`, `last-three-fourths`
)

class RectangleSixthsActions : RectangleWindowActionsWithInlineActions(
  `top-left-sixth`, `top-center-sixth`, `top-right-sixth`, `bottom-left-sixth`, `bottom-center-sixth`, `bottom-right-sixth`
)