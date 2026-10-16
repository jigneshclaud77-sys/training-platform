package com.trainingplatform.enrollments.core.enrollment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.trainingplatform.catalog.api.CourseDto;
import com.trainingplatform.catalog.api.CourseNotFoundException;
import com.trainingplatform.catalog.api.CoursesApi;
import com.trainingplatform.enrollments.api.EnrollmentCreatedEvent;
import com.trainingplatform.enrollments.api.EnrollmentDto;
import com.trainingplatform.enrollments.core.event.EventPublisher;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EnrollmentServiceTest {

    private final EnrollmentRepository repository = mock(EnrollmentRepository.class);
    private final CoursesApi courses = mock(CoursesApi.class);
    private final EventPublisher events = mock(EventPublisher.class);
    private final EnrollmentService service = new EnrollmentService(repository, courses, events);

    private final CreateEnrollmentRequest request = new CreateEnrollmentRequest("Ann", "ann@x.com", 3L);

    @Test
    void enrollSavesAndPublishesEvent() {
        stubCourse(true, 2);
        stubSave(11L);

        EnrollmentDto dto = service.enroll(request);

        assertEquals(new EnrollmentDto(11L, "Ann", "ann@x.com", 3L), dto);
        verify(events).publish(new EnrollmentCreatedEvent(11L, 3L, "Ann", "ann@x.com"));
    }

    @Test
    void enrollPropagatesUnknownCourse() {
        when(courses.findCourse(3L)).thenThrow(new CourseNotFoundException(3L));

        assertThrows(CourseNotFoundException.class, () -> service.enroll(request));
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void enrollRejectsUnpublishedCourse() {
        stubCourse(false, 2);

        assertThrows(CourseNotPublishedException.class, () -> service.enroll(request));
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void enrollRejectsFullCourse() {
        stubCourse(true, 1);
        when(repository.countByCourseId(3L)).thenReturn(1L);

        assertThrows(CourseFullException.class, () -> service.enroll(request));
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void listByCourseMapsEntitiesToDtos() {
        Enrollment enrollment = Enrollment.create(3L, "Ann", "ann@x.com");
        ReflectionTestUtils.setField(enrollment, "id", 11L);
        when(repository.findByCourseId(3L)).thenReturn(List.of(enrollment));

        assertEquals(List.of(new EnrollmentDto(11L, "Ann", "ann@x.com", 3L)), service.listByCourse(3L));
    }

    private void stubCourse(boolean published, int capacity) {
        when(courses.findCourse(3L)).thenReturn(new CourseDto(3L, "Java", capacity, "Berlin", published));
    }

    private void stubSave(long id) {
        when(repository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            ReflectionTestUtils.setField(enrollment, "id", id);
            return enrollment;
        });
    }
}
