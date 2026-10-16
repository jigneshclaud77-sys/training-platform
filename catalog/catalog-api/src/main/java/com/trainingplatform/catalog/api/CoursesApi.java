package com.trainingplatform.catalog.api;

public interface CoursesApi {

    /**
     * @throws CourseNotFoundException if no course with the given id exists
     */
    CourseDto findCourse(long courseId);
}
