package com.trainingplatform.catalog.api;

/**
 * Thrown when a course is requested that does not exist.
 */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(long courseId) {
        super("Course " + courseId + " not found");
    }
}
