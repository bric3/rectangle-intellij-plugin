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
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import io.github.bric3.rectangle.RectangleWindowAction
import io.github.bric3.rectangle.RectangleWindowAction.*
import io.github.bric3.rectangle.RectangleWindowAction.`bottom-half`
import io.github.bric3.rectangle.actions.RectangleActionUtil.patchActionText

abstract class RectangleActionGroup(private val actionNames: List<RectangleWindowAction>) : DefaultActionGroup(), DumbAware {
  constructor(vararg actionNames: RectangleWindowAction) : this(actionNames.toList())
  override fun getActionUpdateThread() = BGT

  override fun update(e: AnActionEvent) {
    patchActionText(e)
  }

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val actionManager = ActionManager.getInstance()

    return actionNames.mapNotNull {
      actionManager.getAction(RectangleAction.actionId(it))
    }.toTypedArray()
  }
}

class RectangleHalvesActionGroup : RectangleActionGroup(
  `left-half`, `right-half`, `center-half`,`top-half`, `bottom-half`
)

class RectangleCornerActionGroup : RectangleActionGroup(
  `top-left`, `top-right`, `bottom-left`, `bottom-right`
)