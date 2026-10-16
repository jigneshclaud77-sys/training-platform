package com.trainingplatform.notifications.core.sender;

import com.trainingplatform.notifications.api.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(String recipient, String message) {
        log.info("Notification to {}: {}", recipient, message);
    }
}
