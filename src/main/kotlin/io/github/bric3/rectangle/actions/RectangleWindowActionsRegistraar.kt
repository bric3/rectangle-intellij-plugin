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

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.Anchor
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.NewUI
import io.github.bric3.rectangle.RectangleAppService
import io.github.bric3.rectangle.RectangleApplicationService
import io.github.bric3.rectangle.RectangleBundle.message
import io.github.bric3.rectangle.RectanglePlugin
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch

object RectangleWindowActionsRegistraar {
  private const val RECTANGLE_TITLE_BAR_ACTION_ID = "rectangle-TitleBar"
  private const val MAIN_TOOLBAR_RIGHT_GROUP_ID = "MainToolbarRight"
  private const val SEARCH_EVERYWHERE_ACTION_ID = "SearchEverywhere"

  private val logger = logger<RectangleWindowActionsRegistraar>()

  private val ideBaselineVersion = ApplicationInfo.getInstance().build.baselineVersion

  fun unregisterActions() {
    val actionManager = ActionManager.getInstance()
    RectangleWindowActionsProvider.actionIds.forEach { actionManager.unregisterAction(it) }
  }

  fun createAndRegisterActions() {
    RectangleApplicationService.getInstance().newChildScope()
      .launch(CoroutineName("RectangleActionsRegistraar")) {
        if (!SystemInfo.isMac) {
          RectangleApplicationService.getInstance()
            .notifyUser(message("rectangle.failure.not-available.text"), NotificationType.ERROR)
          return@launch
        }

        if (!RectangleAppService.getInstance().detected) {
          return@launch
        }

        val actionManager = ActionManager.getInstance()

        if (actionManager.getActionIdList(RectangleAction.ACTION_PREFIX)
            .containsAll(RectangleWindowActionsProvider.actionIds)
        ) {
          return@launch
        }

        RectangleWindowActionsProvider.createActions().forEach { action ->
          logger.debug { "Registering action ${action.id}" }
          actionManager.registerAction(action.id, action, RectanglePlugin.PLUGIN_ID)
        }
      }
  }

  // TODO explore if we can use the new UI API to add a global action to the title bar ?
  private fun registerActionInTitleBar() {
    if (!NewUI.isEnabled()) return

    try {
      val targetGroupId = MAIN_TOOLBAR_RIGHT_GROUP_ID
      val constraints = Constraints(Anchor.BEFORE, SEARCH_EVERYWHERE_ACTION_ID)

      val actionManager = ActionManager.getInstance() as? ActionManagerImpl ?: return
      val group = actionManager.getAction(targetGroupId) as? DefaultActionGroup ?: return
      val action = actionManager.getAction(RECTANGLE_TITLE_BAR_ACTION_ID) ?: return
      if (!group.containsAction(action)) {
        actionManager.addToGroup(group, action, constraints)
      }
    } catch (e: Throwable) {
      logger.warn(e)
    }
  }
}