package com.trainingplatform.enrollments.core.enrollment;

class CourseFullException extends RuntimeException {

    CourseFullException(long courseId) {
        super("Course " + courseId + " is full");
    }
}
