package com.trainingplatform.catalog.core.course;

import com.trainingplatform.catalog.api.CourseDto;
import com.trainingplatform.catalog.api.CoursesApi;
import org.springframework.stereotype.Component;

@Component
class CoursesApiImpl implements CoursesApi {

    private final CourseService service;

    CoursesApiImpl(CourseService service) {
        this.service = service;
    }

    @Override
    public CourseDto findCourse(long courseId) {
        return service.get(courseId);
    }
}
