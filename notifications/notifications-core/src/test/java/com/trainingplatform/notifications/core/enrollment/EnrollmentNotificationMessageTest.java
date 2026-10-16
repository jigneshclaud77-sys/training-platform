package com.trainingplatform.notifications.core.enrollment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.trainingplatform.enrollments.api.EnrollmentCreatedEvent;
import org.junit.jupiter.api.Test;

class EnrollmentNotificationMessageTest {

    @Test
    void messageContainsNameAndCourseId() {
        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(1L, 7L, "Ann", "ann@x.com");

        assertEquals("Hi Ann, you are enrolled in course 7.", EnrollmentNotificationMessage.of(event));
    }
}
