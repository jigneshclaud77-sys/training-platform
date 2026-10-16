package com.trainingplatform.enrollments.core.enrollment;

import com.trainingplatform.catalog.api.CourseDto;
import com.trainingplatform.catalog.api.CoursesApi;
import com.trainingplatform.enrollments.api.EnrollmentCreatedEvent;
import com.trainingplatform.enrollments.api.EnrollmentDto;
import com.trainingplatform.enrollments.core.event.EventPublisher;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final CoursesApi courses;
    private final EventPublisher events;

    EnrollmentService(EnrollmentRepository enrollments, CoursesApi courses, EventPublisher events) {
        this.enrollments = enrollments;
        this.courses = courses;
        this.events = events;
    }

    /**
     * Known limitation: the capacity check and the insert are not locked, so two concurrent
     * requests can both take the last seat.
     */
    @Transactional
    EnrollmentDto enroll(CreateEnrollmentRequest request) {
        CourseDto course = courses.findCourse(request.courseId());
        EnrollmentRules.requirePublished(course);
        EnrollmentRules.requireFreeSeat(course, enrollments.countByCourseId(course.id()));

        Enrollment enrollment = enrollments.save(
                Enrollment.create(course.id(), request.name(), request.email()));
        events.publish(new EnrollmentCreatedEvent(enrollment.getId(), enrollment.getCourseId(),
                enrollment.getName(), enrollment.getEmail()));
        return toDto(enrollment);
    }

    @Transactional(readOnly = true)
    List<EnrollmentDto> listByCourse(long courseId) {
        return enrollments.findByCourseId(courseId).stream()
                .map(EnrollmentService::toDto)
                .toList();
    }

    private static EnrollmentDto toDto(Enrollment enrollment) {
        return new EnrollmentDto(enrollment.getId(), enrollment.getName(), enrollment.getEmail(),
                enrollment.getCourseId());
    }
}
