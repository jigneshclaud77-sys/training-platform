package com.trainingplatform.catalog.core.course;

class CourseNotFoundException extends RuntimeException {

    CourseNotFoundException(long courseId) {
        super("Course " + courseId + " not found");
    }
}
