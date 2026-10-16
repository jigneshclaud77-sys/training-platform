package com.trainingplatform.catalog.core.course;

import com.trainingplatform.catalog.api.CourseDto;
import com.trainingplatform.catalog.api.CourseNotFoundException;
import com.trainingplatform.catalog.api.CoursePublishedEvent;
import com.trainingplatform.catalog.core.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CourseService {

    private final CourseRepository courses;
    private final EventPublisher events;

    CourseService(CourseRepository courses, EventPublisher events) {
        this.courses = courses;
        this.events = events;
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

    @Transactional
    CourseDto publish(long courseId) {
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        course.publish();
        CourseDto dto = toDto(courses.save(course));
        events.publish(new CoursePublishedEvent(courseId));
        return dto;
    }

    private static CourseDto toDto(Course course) {
        return new CourseDto(course.getId(), course.getTitle(), course.getCapacity(),
                course.getCity(), course.isPublished());
    }
}
