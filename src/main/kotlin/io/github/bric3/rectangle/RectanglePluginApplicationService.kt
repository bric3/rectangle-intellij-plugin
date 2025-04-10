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

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.NlsContexts.NotificationContent
import icons.RectangleActionsIcons
import io.github.bric3.rectangle.RectangleBundle.message
import io.github.bric3.rectangle.util.getAppBundleId
import io.github.bric3.rectangle.util.retry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Service(Service.Level.APP)
class RectanglePluginApplicationService(private val pluginAppScope: CoroutineScope) : Disposable {
  val ideBundleId = flow {
    val ideBundleId = withContext(Dispatchers.IO) {
      val homePath = PathManager.getHomePath(true)
      retry { getAppBundleId(Path.of(homePath)) }
    }
    
    // In sandboxed IDE, the IDE is extracted in a way that `mdls` cannot find the bundle id
    emit(
      when (ideBundleId) {
        "(null)" -> null
        else -> ideBundleId
      }
    )
  }
    .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    .shareIn(pluginAppScope, SharingStarted.Lazily, replay = 1)

  fun newChildScope(
    context: CoroutineContext = EmptyCoroutineContext,
    supervisor: Boolean = true,
  ): CoroutineScope {
    val newJob = if (supervisor)
      SupervisorJob(pluginAppScope.coroutineContext.job)
    else
      Job(pluginAppScope.coroutineContext.job)
    return CoroutineScope(pluginAppScope.coroutineContext + newJob + context)
  }

  fun notifyUser(@NotificationContent message: String, notificationType: NotificationType, customizer: Notification.() -> Unit = {}) {
    Notifications.Bus.notify(
      Notification(
        NOTIFICATION_GROUP_ID,
        NOTIFICATION_TITLE,
        message,
        notificationType
      ).also { notification ->
        notification.icon = RectangleActionsIcons.Rectangle
        customizer(notification)
      }
    )
  }

  override fun dispose() {}

  companion object {
    private const val NOTIFICATION_GROUP_ID = "Rectangle"
    private val NOTIFICATION_TITLE = message("rectangle.notification.title")

    fun getInstance() = ApplicationManager.getApplication().service<RectanglePluginApplicationService>()
  }
}