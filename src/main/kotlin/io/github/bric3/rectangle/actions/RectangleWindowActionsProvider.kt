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

import io.github.bric3.rectangle.RectangleWindowActionName

/**
 * > Open the URL `rectangle://execute-action?name=[name]`. Do not activate Rectangle if possible.
 * >
 * > Available values for `[name]`:
 * >   * `left-half`, `right-half`, `center-half`, `top-half`, `bottom-half`,
 * >   * `top-left`, `top-right`, `bottom-left`, `bottom-right`,
 * >   * `first-third`, `center-third`, `last-third`,
 * >   * `first-two-thirds`, `last-two-thirds`,
 * >   * `maximize`, `almost-maximize`, `maximize-height`, `smaller`, `larger`, `center`, `center-prominently`, `restore`,
 * >   * `next-display`, `previous-display`,
 * >   * `move-left`, `move-right`, `move-up`, `move-down`,
 * >   * `first-fourth`, `second-fourth`, `third-fourth`, `last-fourth`, `first-three-fourths`, `last-three-fourths`,
 * >   * `top-left-sixth`, `top-center-sixth`, `top-right-sixth`, `bottom-left-sixth`, `bottom-center-sixth`, `bottom-right-sixth`,
 * >   * `specified`, `reverse-all`,
 * >   * `top-left-ninth`, `top-center-ninth`, `top-right-ninth`, `middle-left-ninth`, `middle-center-ninth`, `middle-right-ninth`, `bottom-left-ninth`, `bottom-center-ninth`, `bottom-right-ninth`,
 * >   * `top-left-third`, `top-right-third`, `bottom-left-third`, `bottom-right-third`,
 * >   * `top-left-eighth`, `top-center-left-eighth`, `top-center-right-eighth`, `top-right-eighth`, `bottom-left-eighth`, `bottom-center-left-eighth`, `bottom-center-right-eighth`, `bottom-right-eighth`,
 * >   * `tile-all`, `cascade-all`, `cascade-active-app`
 * >
 * > Example, from a shell: `open -g "rectangle://execute-action?name=left-half"`
 */
object RectangleWindowActionsProvider {

  fun createActions(): List<RectangleAction> {
    return RectangleWindowActionName.entries.map { RectangleAction(it) }
  }

  val actionIds = RectangleWindowActionName.entries.map { RectangleAction.actionId(it) }
}