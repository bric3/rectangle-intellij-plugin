/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.*

object RectangleActionsIcons {
  private val iconMappings = mutableMapOf<String, Icon>()

  init {
    RectangleActionsIcons::class.nestedClasses.forEach { nestedClass ->
      nestedClass.nestedClasses.forEach { iconClass ->
        iconClass.objectInstance // this will initialize the object and loads the icon
      }
    }
  }

  fun findIcon(name: String): Icon? {
    return iconMappings[name]
  }

  @JvmStatic
  private fun load(path: String): Icon {
    return IconLoader.getIcon(path, RectangleActionsIcons::class.java).also { icon ->
      val fileName = path.substringAfterLast('/').substringBeforeLast(".svg")
      iconMappings[fileName] = icon
    }
  }

  /* Needs to be 16x16 */
  object Actions {
    object Halves {
      /** 16x16 */ @JvmField val TopHalf = load("icons/halves/top-half.svg")
      /** 16x16 */ @JvmField val BottomHalf = load("icons/halves/bottom-half.svg")
      /** 16x16 */ @JvmField val LeftHalf = load("icons/halves/left-half.svg")
      /** 16x16 */ @JvmField val RightHalf = load("icons/halves/right-half.svg")
      /** 16x16 */ @JvmField val CenterHalf = load("icons/halves/center-half.svg")
    }

    object Corners {
      /** 16x16 */ @JvmField val TopLeft = load("icons/corners/top-left.svg")
      /** 16x16 */ @JvmField val TopRight = load("icons/corners/top-right.svg")
      /** 16x16 */ @JvmField val BottomLeft = load("icons/corners/bottom-left.svg")
      /** 16x16 */ @JvmField val BottomRight = load("icons/corners/bottom-right.svg")
    }

    object Thirds {
      /** 16x16 */ @JvmField val FirstThird = load("icons/thirds/first-third.svg")
      /** 16x16 */ @JvmField val CenterThird = load("icons/thirds/center-third.svg")
      /** 16x16 */ @JvmField val LastThird = load("icons/thirds/last-third.svg")
      /** 16x16 */ @JvmField val FirstTwoThirds = load("icons/thirds/first-two-thirds.svg")
      /** 16x16 */ @JvmField val LastTwoThirds = load("icons/thirds/last-two-thirds.svg")

      /** 16x16 */ @JvmField val TopLeftThird = load("icons/thirds/top-left-third.svg")
      /** 16x16 */ @JvmField val TopRightThird = load("icons/thirds/top-right-third.svg")
      /** 16x16 */ @JvmField val BottomLeftThird = load("icons/thirds/bottom-left-third.svg")
      /** 16x16 */ @JvmField val BottomRightThird = load("icons/thirds/bottom-right-third.svg")
    }

    object Fourths {
      /** 16x16 */ @JvmField val FirstFourth = load("icons/fourths/first-fourth.svg")
      /** 16x16 */ @JvmField val SecondFourth = load("icons/fourths/second-fourth.svg")
      /** 16x16 */ @JvmField val ThirdFourth = load("icons/fourths/third-fourth.svg")
      /** 16x16 */ @JvmField val LastFourth = load("icons/fourths/last-fourth.svg")
      /** 16x16 */ @JvmField val FirstThreeFourths = load("icons/fourths/first-three-fourths.svg")
      /** 16x16 */ @JvmField val LastThreeFourths = load("icons/fourths/last-three-fourths.svg")
    }

    object Sixths {
      /** 16x16 */ @JvmField val TopLeftSixth = load("icons/sixths/top-left-sixth.svg")
      /** 16x16 */ @JvmField val TopCenterSixth = load("icons/sixths/top-center-sixth.svg")
      /** 16x16 */ @JvmField val TopRightSixth = load("icons/sixths/top-right-sixth.svg")
      /** 16x16 */ @JvmField val BottomLeftSixth = load("icons/sixths/bottom-left-sixth.svg")
      /** 16x16 */ @JvmField val BottomCenterSixth = load("icons/sixths/bottom-center-sixth.svg")
      /** 16x16 */ @JvmField val BottomRightSixth = load("icons/sixths/bottom-right-sixth.svg")
    }

    object Moves {
      /** 16x16 */ @JvmField val MoveLeft = load("icons/move-to-edge/move-left.svg")
      /** 16x16 */ @JvmField val MoveRight = load("icons/move-to-edge/move-right.svg")
      /** 16x16 */ @JvmField val MoveUp = load("icons/move-to-edge/move-up.svg")
      /** 16x16 */ @JvmField val MoveDown = load("icons/move-to-edge/move-down.svg")
    }

    object General {
      /** 16x16 */ @JvmField val Maximize = load("icons/general/maximize.svg")
      /** 16x16 */ @JvmField val AlmostMaximize = load("icons/general/almost-maximize.svg")
      /** 16x16 */ @JvmField val MaximizeHeight = load("icons/general/maximize-height.svg")
      /** 16x16 */ @JvmField val Smaller = load("icons/general/smaller.svg")
      /** 16x16 */ @JvmField val Larger = load("icons/general/larger.svg")
      /** 16x16 */ @JvmField val Center = load("icons/general/center.svg")
      /** 16x16 */ @JvmField val Restore = load("icons/general/restore.svg")
      /** 16x16 */ @JvmField val CascadeActiveApp = load("icons/general/cascade-active-app.svg")
    }
  }
}