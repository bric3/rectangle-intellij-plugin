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

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.github.bric3.rectangle.RectangleBundle.message
import org.jetbrains.annotations.Nls

object RectangleActionUtil {
  fun AnAction.patchActionText(e: AnActionEvent) {
    val originalText = originalText() ?: return

    e.presentation.text = when (val place = e.place) {
      ActionPlaces.ACTION_SEARCH -> message("rectangle.action.text-with-prefix", originalText)
      else -> originalText
    }
  }

  fun AnAction.originalText(): @Nls String? {
    val id = ActionManager.getInstance().getId(this) ?: return null
    return when(this) {
      is ActionGroup -> message("group.$id.text")
      else -> message("action.$id.text")
    }
  }
}