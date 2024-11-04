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
import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.progress.ProgressIndicator

abstract class Command<T>(
  private val onProcessExecutionException: (Exception) -> T,
  private val onProcessFailure: ProcessOutput.() -> T,
  private val onProcessSuccess: ProcessOutput.() -> T,
) {
  open val progressIndicator: ProgressIndicator? = null
  open val timeout: Int = 1_000

  fun run(): T {
    val commandLine = GeneralCommandLine().apply {
      commandLine()
    }

    val handler = try {
      OSProcessHandler(commandLine)
    } catch (e: Exception) {
      return onProcessExecutionException(e)
    }

    val runner = CapturingProcessRunner(handler)
    // inspired by git4idea.config.GitVersion#identifyVersion
    // check obsolescence
    val output = progressIndicator?.let { runner.runProcess(it) } ?: runner.runProcess(timeout)

    return if (output.isTimeout || output.exitCode != 0) {
      onProcessFailure(output)
    } else {
      onProcessSuccess(output)
    }
  }

  abstract fun GeneralCommandLine.commandLine()
}