/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.rectangle.util

import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * [RotatedIcon] allows you to rotate Icon by wanted degrees.
 *
 * @param icon     the Icon to rotate
 * @param degrees  the degree of rotation
 */
class RotatedIcon(
  val icon: Icon,
  var degrees: Double = 0.0,
) : Icon {
  override fun getIconWidth(): Int = length(icon.iconWidth, icon.iconHeight)
  override fun getIconHeight(): Int = length(icon.iconHeight, icon.iconWidth)

  private fun length(wantedSideLength: Int, otherSideLength: Int): Int {
    val radians = Math.toRadians(degrees)
    val sin = abs(sin(radians))
    val cos = abs(cos(radians))
    return floor(wantedSideLength * cos + otherSideLength * sin).toInt()
  }

  override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
    val g2 = g.create() as Graphics2D

    val cWidth = icon.iconWidth / 2
    val cHeight = icon.iconHeight / 2

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setClip(x, y, iconWidth, iconHeight)
    g2.translate((iconWidth - icon.iconWidth) / 2, (iconHeight - icon.iconHeight) / 2)
    g2.rotate(Math.toRadians(degrees), (x + cWidth).toDouble(), (y + cHeight).toDouble())
    icon.paintIcon(c, g2, x, y)

    g2.dispose()
  }
}