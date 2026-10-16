package com.trainingplatform.enrollments.core.enrollment;

class CourseNotPublishedException extends RuntimeException {

    CourseNotPublishedException(long courseId) {
        super("Course " + courseId + " is not published");
    }
}
