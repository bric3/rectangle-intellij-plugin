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

import io.github.bric3.rectangle.RectanglePlugin
import java.nio.file.Path

/**
 * Runs `mdls -attr kMDItemCFBundleIdentifier -raw ....app`
 */
fun getAppBundleId(path: Path) = MdlsCommand(
  appPath = path.toString(),
  attributeName = "kMDItemCFBundleIdentifier",
  onProcessExecutionException = {
    RectanglePlugin.logger.error("Failed to get bundle id for $path", it)
    null
  },
  onProcessFailure = {
    RectanglePlugin.logger.error("Failed to get bundle id for $path: $stderr")
    null
  },
  onProcessSuccess = { stdout.trim() }
).run()