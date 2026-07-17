/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package rectangle.yumi

import dev.yumi.gradle.licenser.api.comment.HeaderComment

object PropertiesStyleHeaderComment : HeaderComment {
  override fun readHeaderComment(content: String): HeaderComment.Result {
    val separator = extractLineSeparator(content)
    val existing = mutableListOf<String>()
    var end = 0

    while (end < content.length) {
      val nextEnd = content.indexOf('\n', end).let { if (it == -1) content.length else it + 1 }
      val line = content.substring(end, nextEnd).removeSuffix("\n").removeSuffix("\r")
      if (!line.startsWith("#")) {
        break
      }

      existing += line.removePrefix("#").removePrefix(" ")
      end = nextEnd
    }

    if (existing.isEmpty()) {
      return HeaderComment.Result(0, 0, null, separator)
    }
    if (existing.first().isBlank()) {
      existing.removeAt(0)
    }
    if (existing.lastOrNull()?.isBlank() == true) {
      existing.removeAt(existing.lastIndex)
    }

    return HeaderComment.Result(0, end, existing, separator)
  }

  override fun writeHeaderComment(lines: MutableList<String>, lineSeparator: String): String = buildString {
    append("#").append(lineSeparator)
    lines.forEach { line ->
      if (line.isBlank()) {
        append("#")
      } else {
        append("# ").append(line)
      }
      append(lineSeparator)
    }
    append("#").append(lineSeparator)
  }
}