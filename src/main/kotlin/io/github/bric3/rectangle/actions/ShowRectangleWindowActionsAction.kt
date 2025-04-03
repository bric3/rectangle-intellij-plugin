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

import com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.SPEEDSEARCH
import com.intellij.openapi.wm.IdeFocusManager
import icons.RectangleActionsIcons
import io.github.bric3.rectangle.RectangleAppService
import io.github.bric3.rectangle.actions.RectangleActionUtil.patchActionText
import org.jdesktop.swingx.VerticalLayout
import javax.swing.JPanel

class ShowRectangleWindowActionsAction : DumbAwareAction() {
  override fun getActionUpdateThread() = BGT

  override fun update(e: AnActionEvent) {
    patchActionText(e)
    e.presentation.icon = RectangleActionsIcons.RectangleMenu
    e.presentation.isEnabledAndVisible = RectangleAppService.getInstance().detectedFlow.value
  }

  override fun actionPerformed(e: AnActionEvent) {
    popupMenuWithInlines(e)

    // popupWithCustomContent(e)
  }

  @Suppress("UnstableApiUsage")
  private fun popupWithCustomContent(e: AnActionEvent) {
    val frame = IdeFocusManager.getGlobalInstance().lastFocusedFrame ?: return

    val halves = ActionUtil.createToolbarComponent(
      frame.component,
      "RectangleMenu",
      ActionUtil.getActionGroup("rectangle.HalvesGroup") ?: return,
      true
    )

    val corners = ActionUtil.createToolbarComponent(
      frame.component,
      "RectangleMenu",
      ActionUtil.getActionGroup("rectangle.CornersGroup") ?: return,
      true
    )

    val content = JPanel(VerticalLayout(2)).apply {
      add(halves)
      add(corners)
    }

    JBPopupFactory.getInstance()
      .createComponentPopupBuilder(content, content)
      .setAdText("Click an action")
      .setTitle("Rectangle")
      .createPopup()
      .showInBestPositionForEvent(e)
  }

  private fun popupMenuWithInlines(e: AnActionEvent) {
    @Suppress("UnstableApiUsage")
    val actionGroup = ActionUtil.getActionGroup("rectangle.Menu") ?: return
    val popup = JBPopupFactory.getInstance()
      .createActionGroupPopup(
        "Rectangle Actions",
        actionGroup,
        e.dataContext,
        SPEEDSEARCH,
        true
      )

    popup.showInBestPositionForEvent(e)
  }

  private fun JBPopup.showInBestPositionForEvent(e: AnActionEvent) {
    e.inputEvent?.component?.let {
      showUnderneathOf(it)
    } ?: IdeFocusManager.getGlobalInstance().lastFocusedFrame?.component?.let {
      showInCenterOf(it)
    } ?: showInBestPositionFor(e.dataContext)
  }
}