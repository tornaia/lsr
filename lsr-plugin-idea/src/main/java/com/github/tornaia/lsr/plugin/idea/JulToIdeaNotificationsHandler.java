package com.github.tornaia.lsr.plugin.idea;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JulToIdeaNotificationsHandler extends Handler {

    private static final String NOTIFICATION_GROUP_TYPE = "Large Scale Refactor";

    public static void install() {
        Notifications.Bus.register(NOTIFICATION_GROUP_TYPE, NotificationDisplayType.NONE);

        Logger logger = Logger.getLogger("com.github.tornaia");
        logger.setLevel(Level.ALL);
        logger.addHandler(new JulToIdeaNotificationsHandler());
    }

    @Override
    public void publish(LogRecord record) {
        if (Objects.isNull(record)) {
            return;
        }

        String title = record.getLoggerName();
        String message = record.getMessage();
        NotificationType type = convert(record.getLevel());
        Notification notification = new Notification(NOTIFICATION_GROUP_TYPE, title, message, type);
        Notifications.Bus.notify(notification);
    }

    private static NotificationType convert(Level level) {
        switch (level.getName()) {
            case "SEVERE":
                return NotificationType.ERROR;
            case "WARNING":
                return NotificationType.WARNING;
            default:
                return NotificationType.INFORMATION;
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
