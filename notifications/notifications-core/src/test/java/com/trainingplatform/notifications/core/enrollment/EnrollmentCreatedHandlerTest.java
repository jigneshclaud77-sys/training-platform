package com.trainingplatform.notifications.core.enrollment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.trainingplatform.enrollments.api.EnrollmentCreatedEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnrollmentCreatedHandlerTest {

    private final EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(1L, 7L, "Ann", "ann@x.com");

    @Test
    void sendsConfirmationToEnrolledEmail() {
        List<String> sent = new ArrayList<>();
        EnrollmentCreatedHandler handler = new EnrollmentCreatedHandler(
                (recipient, message) -> sent.add(recipient + "|" + message));

        handler.on(event);

        assertEquals(List.of("ann@x.com|Hi Ann, you are enrolled in course 7."), sent);
    }

    @Test
    void failingSenderDoesNotPropagate() {
        EnrollmentCreatedHandler handler = new EnrollmentCreatedHandler((recipient, message) -> {
            throw new IllegalStateException("channel down");
        });

        assertDoesNotThrow(() -> handler.on(event));
    }
}
