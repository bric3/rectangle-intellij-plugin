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

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.NlsActions.ActionText
import com.intellij.util.text.SemVer
import io.github.bric3.rectangle.DefaultsOp.SettingsKey
import io.github.bric3.rectangle.DragSnapTweak.IgnoreDragSnapToo
import io.github.bric3.rectangle.RectangleBundle.message
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

object DragSnapTweak {
  /**
   * [Minimum version of Rectangle](https://github.com/rxhanson/Rectangle/releases/tag/v0.85) that supports ignoring apps.
   *
   * Note Rectangle versions do not follow semver exactly so this version was
   * suffixed with the patch number.
   */
  private val minRectangleVersionWithIgnoreAppTask = SemVer.parseFromText("0.85.0")

  /**
   * Since Rectangle, ignoring an app, actually ignores Rectangle shortcuts when that app is
   * focused. However, dragging and snapping are also disabled. This function suggests the user
   * to both enable the drag snap feature for ignored apps, and ignore the running IDE.
   */
  fun suggestEnablingDragSnapWhenAppIgnored() {
    RectanglePluginApplicationService.getInstance().newChildScope().launch {
      RectangleAppService.getInstance().detectedVersionFlow
        .filterNotNull()
        .collectLatest {
          ignoreIdeInRectangle(it)
        }
    }
  }

  // TODO internal action to test notification
  private suspend fun ignoreIdeInRectangle(rectangleVersion: SemVer, forceNotification: Boolean = false) {
    RectanglePluginApplicationService.getInstance().ideBundleId.collectLatest { ideBundleIdentifier ->
      val ignoreDragSnapToo = RectangleAppService.getInstance().rectangleDefaults(DefaultsOp.ReadOp(IgnoreDragSnapToo))
      if (ignoreDragSnapToo.value != false || forceNotification) {
        RectanglePluginApplicationService.getInstance().notifyUser(
          message("rectangle.notification.drag-snap-ignored.title", ApplicationNamesInfo.getInstance().fullProductName),
          NotificationType.INFORMATION,
        ) {
          if (ideBundleIdentifier != null && rectangleVersion >= minRectangleVersionWithIgnoreAppTask) {
            addAction(AllowDragSnapForIgnoredAppsAction(
              message("rectangle.notification.ignore-app.and.drag-snap-ignored.suggested-action.enable"),
              this,
              alsoIgnoreApp = true,
              ideBundleIdentifier,
            ))
          }

          addAction(AllowDragSnapForIgnoredAppsAction(
            message("rectangle.notification.drag-snap-ignored.suggested-action.enable"),
            this,
          ))
        }
      }
      // TODO Auto ignore ide?
      //  Or detect if the IDE is already ignored and ask.
      //  May need support from Rectangle.
    }
  }

  /**
   * IgnoreDragSnapToo defaults settings key.
   *
   * [Rectangle/Rectangle/Defaults.swift](https://github.com/rxhanson/Rectangle/blob/b841afb185c888072678331f83d242b028bce927/Rectangle/Defaults.swift#L254-L261)
   *
   * Lines 254 to 261 in b841afb
   *  private func set(using intValue: Int) {
   *      switch intValue {
   *      case 0: enabled = nil
   *      case 1: enabled = true
   *      case 2: enabled = false
   *      default: break
   *      }
   *  }
   */
  object IgnoreDragSnapToo : SettingsKey<Boolean> {
    override val key = "ignoreDragSnapToo"
    override val typeParam = "-int"

    override fun fromString(output: String): Boolean {
      return when (output) {
        "0" -> true // default
        "1" -> true
        "2" -> false
        else -> error("Unexpected value for $key: $output")
      }
    }

    override fun toString(value: Boolean): String {
      return when (value) {
        true -> "1"
        false -> "2"
      }
    }
  }
}

class AllowDragSnapForIgnoredAppsAction(
  @ActionText actionTitle: String,
  private val notification: Notification,
  private val alsoIgnoreApp: Boolean = false,
  private val ideBundleIdentifier: String? = null,
) : DumbAwareAction(actionTitle) {
  override fun actionPerformed(e: AnActionEvent) {
    RectanglePluginApplicationService.getInstance().newChildScope().launch {
      if (alsoIgnoreApp && ideBundleIdentifier != null) {
        RectangleAppService.getInstance().runRectangleUrlTask(
          RectangleTask.`ignore-app`,
          mapOf("app-bundle-id" to ideBundleIdentifier)
        )
      }

      val setRectangleDefaults = RectangleAppService.getInstance().rectangleDefaults(
        DefaultsOp.WriteOp(IgnoreDragSnapToo, false)
      )

      if (setRectangleDefaults.isSuccessful) {
        notification.expire()

        RectanglePluginApplicationService.getInstance().notifyUser(
          message("rectangle.notification.drag-snap-ignored.next-step.enabled"),
          NotificationType.INFORMATION
        )
      }
    }
  }
}