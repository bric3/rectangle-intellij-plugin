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

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import io.github.bric3.rectangle.actions.RectangleWindowActionsRegistraar
import io.github.bric3.rectangle.actions.RectangleWindowActionsRegistraar.createAndRegisterActions

class RectanglePluginLifecycleListener : AppLifecycleListener, DynamicPluginListener {
  override fun appFrameCreated(commandLineArgs: MutableList<String>) {
    createAndRegisterActions()

    // discover if Rectangle has ignoreDragSnapToo
    DragSnapTweak.suggestEnablingDragSnapWhenAppIgnored()
  }

  override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
    if (pluginDescriptor.pluginId == RectanglePlugin.PLUGIN_ID) {
      createAndRegisterActions()

      // discover if Rectangle has ignoreDragSnapToo
      DragSnapTweak.suggestEnablingDragSnapWhenAppIgnored()
    }
  }

  override fun pluginUnloaded(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
    if (pluginDescriptor.pluginId == RectanglePlugin.PLUGIN_ID) {
      RectangleWindowActionsRegistraar.unregisterActions()
    }
  }
}