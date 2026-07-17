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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginId

object RectanglePlugin {
  /**
   * Global plugin logger, for util functions.
   */
  val logger = thisLogger()
  const val PLUGIN_ID_STR = "io.github.bric3.rectangle"
  val PLUGIN_ID = PluginId.getId(PLUGIN_ID_STR)
}
