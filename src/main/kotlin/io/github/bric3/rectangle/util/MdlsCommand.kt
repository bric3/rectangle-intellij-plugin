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

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput

class MdlsCommand<T>(
  private val appPath: String,
  private val attributeName: String,
  onProcessExecutionException: (Exception) -> T,
  onProcessFailure: ProcessOutput.() -> T,
  onProcessSuccess: ProcessOutput.() -> T,
) : Command<T>(
  onProcessExecutionException,
  onProcessFailure,
  onProcessSuccess
) {
  override fun GeneralCommandLine.commandLine() {
    exePath = "/usr/bin/mdls"
    addParameters("-attr", attributeName)
    addParameter("-raw")
    addParameter(appPath)
  }
}