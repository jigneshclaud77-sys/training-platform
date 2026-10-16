package com.trainingplatform.enrollments.core.enrollment;

import com.trainingplatform.catalog.api.CourseDto;

final class EnrollmentRules {

    private EnrollmentRules() {
    }

    static void requirePublished(CourseDto course) {
        if (!course.published()) {
            throw new CourseNotPublishedException(course.id());
        }
    }

    static void requireFreeSeat(CourseDto course, long enrolledCount) {
        if (enrolledCount >= course.capacity()) {
            throw new CourseFullException(course.id());
        }
    }
}
