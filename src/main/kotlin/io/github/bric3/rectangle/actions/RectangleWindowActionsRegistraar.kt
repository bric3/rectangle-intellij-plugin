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
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfo
import io.github.bric3.rectangle.RectangleAppService
import io.github.bric3.rectangle.RectanglePluginApplicationService
import io.github.bric3.rectangle.RectangleBundle.message
import io.github.bric3.rectangle.RectanglePlugin
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

object RectangleWindowActionsRegistraar {
  private val logger = logger<RectangleWindowActionsRegistraar>()

  @Suppress("unused")
  private val ideBaselineVersion = ApplicationInfo.getInstance().build.baselineVersion

  fun unregisterActions() {
    val actionManager = ActionManager.getInstance()
    RectangleWindowActionsProvider.actionIds.forEach { actionManager.unregisterAction(it) }
  }

  fun createAndRegisterActions() {
    RectanglePluginApplicationService.getInstance().newChildScope()
      .launch(CoroutineName("RectangleActionsRegistraar")) {
        if (!SystemInfo.isMac) {
          RectanglePluginApplicationService.getInstance()
            .notifyUser(message("rectangle.failure.not-available.text"), NotificationType.ERROR)
          return@launch
        }

        // Try creating and registering actions as soon as rectangle is detected, might take a few seconds after retry
        RectangleAppService.getInstance().detectedFlow
          .filter { it }
          .collectLatest {
            val actionManager = ActionManager.getInstance()

            if (actionManager.getActionIdList(RectangleAction.ACTION_PREFIX)
                .containsAll(RectangleWindowActionsProvider.actionIds)
            ) {
              return@collectLatest
            }

            RectangleWindowActionsProvider.createActions().forEach { action ->
              logger.debug { "Registering action ${action.id}" }
              actionManager.registerAction(action.id, action, RectanglePlugin.PLUGIN_ID)
            }
          }
      }
  }
}
