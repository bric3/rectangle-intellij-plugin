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

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.notification.NotificationType.ERROR
import com.intellij.openapi.diagnostic.thisLogger
import io.github.bric3.rectangle.RectangleBundle.message

interface DefaultsOp {
  val name: String
  fun applyParameters(generalCommandLine: GeneralCommandLine)
  fun handleFailure(output: ProcessOutput) {
    thisLogger().error("Failed to run defaults op $name: ${output.stderr}")
    RectanglePluginApplicationService.getInstance()
      .notifyUser(message("rectangle.action.failure.defaults-$name.text"), ERROR)
  }

  fun handleSuccess(output: ProcessOutput)

  val isSuccessful: Boolean

  class ReadOp<T>(private val settingKey: SettingsKey<T>) : DefaultsOp {
    override val name = "read-${settingKey.key}"

    private var _value: T? = null
    val value: T?
      get() = _value

    private var _isSuccessful = false
    override val isSuccessful: Boolean
      get() = _isSuccessful

    override fun applyParameters(generalCommandLine: GeneralCommandLine) {
      generalCommandLine.addParameter("read")
      generalCommandLine.addParameter("com.knollsoft.Rectangle")
      generalCommandLine.addParameter(settingKey.key)
      generalCommandLine.addParameter(settingKey.typeParam)
    }

    override fun handleSuccess(output: ProcessOutput) {
      _value = settingKey.fromString(output.stdout.trim())
      _isSuccessful = true
    }
  }

  class WriteOp<T>(private val settingKey: SettingsKey<T>, private val value: T) : DefaultsOp {
    override val name = "write-${settingKey.key}"

    private var _isSuccessful = false
    override val isSuccessful
      get() = _isSuccessful

    override fun applyParameters(generalCommandLine: GeneralCommandLine) {
      generalCommandLine.addParameter("write")
      generalCommandLine.addParameter("com.knollsoft.Rectangle")
      generalCommandLine.addParameter(settingKey.key)
      generalCommandLine.addParameter(settingKey.typeParam)
      generalCommandLine.addParameter(settingKey.toString(value))
    }

    override fun handleSuccess(output: ProcessOutput) {
      _isSuccessful = true
    }
  }

  interface SettingsKey<T> {
    val key: String
    val typeParam: String
    fun fromString(output: String): T?
    fun toString(value: T): String
  }
}