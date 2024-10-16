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

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

object RectangleBundle {
  private const val BUNDLE_NAME = "messages.RectangleBundle"

  private val INSTANCE = DynamicBundle(RectanglePlugin::class.java, BUNDLE_NAME)

  @JvmStatic
  fun message(key: @PropertyKey(resourceBundle = BUNDLE_NAME) @NonNls String, vararg params: Any): @Nls String {
    return INSTANCE.getMessage(key, *params)
  }

  @JvmStatic
  fun messagePointer(
    key: @PropertyKey(resourceBundle = BUNDLE_NAME) @NonNls String,
    vararg params: Any
  ): Supplier<@Nls String> {
    return INSTANCE.getLazyMessage(key, *params)
  }
}