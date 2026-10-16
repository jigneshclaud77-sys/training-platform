package com.trainingplatform.enrollments.core.enrollment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EnrollmentTest {

    @Test
    void createTrimsNameAndEmail() {
        Enrollment enrollment = Enrollment.create(5L, "  Ann ", " ann@x.com ");

        assertEquals(5L, enrollment.getCourseId());
        assertEquals("Ann", enrollment.getName());
        assertEquals("ann@x.com", enrollment.getEmail());
    }

    @Test
    void createRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Enrollment.create(5L, " ", "ann@x.com"));
        assertThrows(IllegalArgumentException.class, () -> Enrollment.create(5L, null, "ann@x.com"));
    }

    @Test
    void createRejectsBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> Enrollment.create(5L, "Ann", ""));
        assertThrows(IllegalArgumentException.class, () -> Enrollment.create(5L, "Ann", null));
    }
}
