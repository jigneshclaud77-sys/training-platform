package com.trainingplatform.catalog.core.course;

import com.trainingplatform.catalog.api.CourseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CourseService {

    private final CourseRepository courses;

    CourseService(CourseRepository courses) {
        this.courses = courses;
    }

    @Transactional
    CourseDto create(CreateCourseRequest request) {
        Course course = Course.create(request.title(), request.capacity(), request.city());
        return toDto(courses.save(course));
    }

    @Transactional(readOnly = true)
    CourseDto get(long courseId) {
        return courses.findById(courseId)
                .map(CourseService::toDto)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private static CourseDto toDto(Course course) {
        return new CourseDto(course.getId(), course.getTitle(), course.getCapacity(),
                course.getCity(), course.isPublished());
    }
}
