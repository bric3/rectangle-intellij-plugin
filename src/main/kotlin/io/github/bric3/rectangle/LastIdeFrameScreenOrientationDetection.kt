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

import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.ScreenUtil
import com.intellij.util.concurrency.annotations.RequiresEdt

object LastIdeFrameScreenOrientationDetection {
  @RequiresEdt
  fun isPortrait(): Boolean {
    val frame = IdeFocusManager.getGlobalInstance().lastFocusedFrame ?: return false
    val screenRectangle = ScreenUtil.getScreenRectangle(frame.component).bounds
    return screenRectangle.width < screenRectangle.height
  }
}