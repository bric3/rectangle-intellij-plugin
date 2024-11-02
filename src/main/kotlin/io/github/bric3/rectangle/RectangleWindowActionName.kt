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

import com.intellij.openapi.util.text.StringUtil
import io.github.bric3.rectangle.RectangleBundle.messagePointer
import io.github.bric3.rectangle.RectangleWindowActionName.Category

@Suppress("EnumEntryName")
enum class RectangleWindowActionName {
  `left-half`, `right-half`, `center-half`, `top-half`, `bottom-half`,
  `top-left`, `top-right`, `bottom-left`, `bottom-right`,
  `first-third`, `center-third`, `last-third`,
  `first-two-thirds`, `last-two-thirds`,
  maximize, `almost-maximize`, `maximize-height`, smaller, larger, center, `center-prominently`, restore,
  `next-display`, `previous-display`,
  `move-left`, `move-right`, `move-up`, `move-down`,
  `first-fourth`, `second-fourth`, `third-fourth`, `last-fourth`, `first-three-fourths`, `last-three-fourths`,
  `top-left-sixth`, `top-center-sixth`, `top-right-sixth`, `bottom-left-sixth`, `bottom-center-sixth`, `bottom-right-sixth`,

  //    specified, `reverse-all`,
  `top-left-ninth`, `top-center-ninth`, `top-right-ninth`, `middle-left-ninth`, `middle-center-ninth`, `middle-right-ninth`, `bottom-left-ninth`, `bottom-center-ninth`, `bottom-right-ninth`,
  `top-left-third`, `top-right-third`, `bottom-left-third`, `bottom-right-third`,
  `top-left-eighth`, `top-center-left-eighth`, `top-center-right-eighth`, `top-right-eighth`, `bottom-left-eighth`, `bottom-center-left-eighth`, `bottom-center-right-eighth`, `bottom-right-eighth`,

  //    `tile-all`, `cascade-all`,
  `cascade-active-app`
  ;

  fun toTitleCase() = StringUtil.toTitleCase(name).replace("-", " ")
  fun toId() = StringUtil.toTitleCase(name).replace("-", "")
  fun description() = messagePointer("rectangle.action.${name}.description")

  enum class Category(vararg val actionNames: RectangleWindowActionName) {
    Halves(`left-half`, `right-half`, `center-half`, `top-half`, `bottom-half`),
    Corners(`top-left`, `top-right`, `bottom-left`, `bottom-right`),
    Thirds(`first-third`, `center-third`, `last-third`, `first-two-thirds`, `last-two-thirds`),
    Fourths(
      `first-fourth`,
      `second-fourth`,
      `third-fourth`,
      `last-fourth`,
      `first-three-fourths`,
      `last-three-fourths`
    ),
    Sixths(
      `top-left-sixth`,
      `top-center-sixth`,
      `top-right-sixth`,
      `bottom-left-sixth`,
      `bottom-center-sixth`,
      `bottom-right-sixth`
    ),
    Ninths(
      `top-left-ninth`, `top-center-ninth`, `top-right-ninth`,
      `middle-left-ninth`, `middle-center-ninth`, `middle-right-ninth`,
      `bottom-left-ninth`, `bottom-center-ninth`, `bottom-right-ninth`
    ),
    Eighths(
      `top-left-eighth`, `top-center-left-eighth`, `top-center-right-eighth`, `top-right-eighth`,
      `bottom-left-eighth`, `bottom-center-left-eighth`, `bottom-center-right-eighth`, `bottom-right-eighth`
    ),
    Move(`move-left`, `move-right`, `move-up`, `move-down`),
    General(maximize, `almost-maximize`, `maximize-height`, smaller, larger, center, restore),
    Display(`previous-display`, `next-display`),
    Unassigned;
  }
}

private val orientableCategories = setOf(Category.Thirds, Category.Fourths, Category.Sixths, Category.Ninths, Category.Eighths)
val RectangleWindowActionName.isOrientable
  get() = orientableCategories.any { it.actionNames.contains(this) }
