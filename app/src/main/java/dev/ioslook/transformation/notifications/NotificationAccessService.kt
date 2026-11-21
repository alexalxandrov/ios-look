package dev.ioslook.transformation.notifications

import android.service.notification.NotificationListenerService

/**
 * Registration only. This initial service deliberately does not inspect,
 * copy, persist, or forward notification contents.
 */
class NotificationAccessService : NotificationListenerService()
