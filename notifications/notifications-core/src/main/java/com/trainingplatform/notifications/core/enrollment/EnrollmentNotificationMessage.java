package com.trainingplatform.notifications.core.enrollment;

import com.trainingplatform.enrollments.api.EnrollmentCreatedEvent;

final class EnrollmentNotificationMessage {

    private EnrollmentNotificationMessage() {
    }

    static String of(EnrollmentCreatedEvent event) {
        return "Hi " + event.name() + ", you are enrolled in course " + event.courseId() + ".";
    }
}
