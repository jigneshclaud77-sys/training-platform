package com.trainingplatform.enrollments.core.enrollment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.trainingplatform.catalog.api.CourseDto;
import org.junit.jupiter.api.Test;

class EnrollmentRulesTest {

    private static final CourseDto PUBLISHED = new CourseDto(1L, "Java", 2, "Berlin", true);
    private static final CourseDto UNPUBLISHED = new CourseDto(1L, "Java", 2, "Berlin", false);

    @Test
    void unpublishedCourseIsRejected() {
        assertThrows(CourseNotPublishedException.class, () -> EnrollmentRules.requirePublished(UNPUBLISHED));
    }

    @Test
    void publishedCourseIsAccepted() {
        assertDoesNotThrow(() -> EnrollmentRules.requirePublished(PUBLISHED));
    }

    @Test
    void fullCourseIsRejected() {
        assertThrows(CourseFullException.class, () -> EnrollmentRules.requireFreeSeat(PUBLISHED, 2));
    }

    @Test
    void lastFreeSeatIsAccepted() {
        assertDoesNotThrow(() -> EnrollmentRules.requireFreeSeat(PUBLISHED, 1));
    }
}
