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
import com.intellij.openapi.project.DumbAwareAction
import io.github.bric3.rectangle.DefaultsOp.ReadOp
import io.github.bric3.rectangle.DefaultsOp.SettingsKey
import io.github.bric3.rectangle.DragSnapTweak.IgnoreDragSnapToo
import io.github.bric3.rectangle.RectangleBundle.message
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

object DragSnapTweak {
  fun suggestEnablingDragSnapWhenAppIgnored() {
    if (!RectangleAppService.getInstance().detectedFlow.value) {
      return
    }

    RectanglePluginApplicationService.getInstance().newChildScope().launch {
      RectangleAppService.getInstance().detectedFlow.filter { it }.collectLatest {
        // TODO use 0.86
        if (RectangleAppService.getInstance().rectangleDefaults(ReadOp(IgnoreDragSnapToo)).value != false) {
          RectanglePluginApplicationService.getInstance().notifyUser(
            message("rectangle.notification.drag-snap-ignored.title"),
            NotificationType.INFORMATION,
          ) {
            addAction(AllowDragSnapForIgnoredAppsAction(this))
          }
        }
      }
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

    override fun fromString(output: String): Boolean? {
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

class AllowDragSnapForIgnoredAppsAction(private val notification: Notification) : DumbAwareAction(message("rectangle.notification.drag-snap-ignored.suggested-action.enable")) {
  override fun actionPerformed(e: AnActionEvent) {
    RectanglePluginApplicationService.getInstance().newChildScope().launch {
      RectangleAppService.getInstance().rectangleDefaults(
        DefaultsOp.WriteOp(IgnoreDragSnapToo, false)
      ).run {
        if (isSuccessful) {
          notification.expire()

          RectanglePluginApplicationService.getInstance().notifyUser(
            message("rectangle.notification.drag-snap-ignored.next-step.enabled"),
            NotificationType.INFORMATION
          )
        }
      }
    }
  }
}