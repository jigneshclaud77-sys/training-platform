package com.trainingplatform.notifications.core.enrollment;

import com.trainingplatform.enrollments.api.EnrollmentCreatedEvent;
import com.trainingplatform.notifications.api.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class EnrollmentCreatedHandler {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentCreatedHandler.class);

    private final NotificationSender sender;

    EnrollmentCreatedHandler(NotificationSender sender) {
        this.sender = sender;
    }

    /**
     * Runs synchronously inside the enrollment transaction, so a failing channel is caught here
     * and must never fail the enrollment request.
     */
    @EventListener
    void on(EnrollmentCreatedEvent event) {
        try {
            sender.send(event.email(), EnrollmentNotificationMessage.of(event));
        } catch (RuntimeException e) {
            log.warn("Could not notify enrollment {}", event.enrollmentId(), e);
        }
    }
}
