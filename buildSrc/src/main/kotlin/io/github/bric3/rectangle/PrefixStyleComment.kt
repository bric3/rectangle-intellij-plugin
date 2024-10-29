package io.github.bric3.rectangle/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import dev.yumi.gradle.licenser.api.comment.HeaderComment
import dev.yumi.gradle.licenser.util.Utils

/**
 * Could be used for various prefixes (`;`, `#`, `@rem`, etc)
 *
 * Use this way
 * ```
 *   headerCommentManager.register(
 *     setOf("toml"),
 *     io.github.bric3.rectangle.PrefixStyleComment("#")
 *   )
 * ```
 */
class PrefixStyleComment(private val prefix: String) : HeaderComment {
  private val prefixLength = prefix.length
  override fun readHeaderComment(source: String): HeaderComment.Result {
    var start = 0
    var end: Int
    var found: String? = null

    end = 0
    while (end < source.length) {
      val c = source[end]
      val substring = source.substring(end, prefixLength)

      if (substring == prefix) {
        // TODO
      }

      // TODO original code

      // Find comment start.
      if (c == '/') {
        if (Utils.matchCharAt(source, end + 1, '*')
          && Utils.matchOtherCharAt(source, end + 2, '*')
        ) {
          // Header!
          start = end
          var j = end + 2

          // Attempt to find the end of it.
          while (j < source.length) {
            j = source.indexOf('*', j + 1)

            if (j == -1) {
              found = source.substring(end + 2)
              break
            }

            if (j + 1 == source.length) {
              found = source.substring(end + 2)
              end = j
              break
            }

            if (source.get(j + 1) == '/') {
              // The end!
              found = source.substring(end + 2, j - 1)
              end = j + 2
              break
            }
          }
        } else break

        if (found != null) break
      } else if (!Character.isWhitespace(c)) break
      end++
    }

    val separator = this.extractLineSeparator(source)
    var result: MutableList<String?>? = null

    if (found != null) {
      val lines: Array<String?> = found.split("\r?\n( ?\\* ?)?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      lines[0] = lines[0]!!.trimStart()

      result = mutableListOf(*lines)

      if (result[0]!!.isBlank()) {
        result.removeAt(0)
      }
    }

    return HeaderComment.Result(start, end, result, separator)
  }

  override fun writeHeaderComment(
    header: List<String>,
    separator: String
  ): String {
    return buildString {
      append(prefix).append(separator)
      header.forEach {
        append(prefix)
        if (it.isNotBlank()) {
          append(' ').append(it)
        }
        append(separator)
      }
      append(prefix).append(separator)
    }
  }
}
