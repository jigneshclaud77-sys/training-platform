package com.trainingplatform.catalog.core.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.trainingplatform.catalog.api.CourseDto;
import com.trainingplatform.catalog.api.CourseNotFoundException;
import com.trainingplatform.catalog.api.CoursePublishedEvent;
import com.trainingplatform.catalog.core.event.EventPublisher;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CourseServiceTest {

    private final CourseRepository repository = mock(CourseRepository.class);
    private final EventPublisher events = mock(EventPublisher.class);
    private final CourseService service = new CourseService(repository, events);

    @Test
    void createMapsSavedCourseToDto() {
        when(repository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            ReflectionTestUtils.setField(course, "id", 7L);
            return course;
        });

        CourseDto dto = service.create(new CreateCourseRequest("Java", 10, "Berlin"));

        assertEquals(new CourseDto(7L, "Java", 10, "Berlin", false), dto);
        assertFalse(dto.published());
    }

    @Test
    void publishReturnsPublishedDto() {
        stubExistingCourse(7L);

        CourseDto dto = service.publish(7L);

        assertEquals(new CourseDto(7L, "Java", 10, "Berlin", true), dto);
    }

    @Test
    void publishEmitsCoursePublishedEvent() {
        stubExistingCourse(7L);

        service.publish(7L);

        verify(events).publish(new CoursePublishedEvent(7L));
    }

    @Test
    void publishThrowsWhenCourseIsUnknown() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> service.publish(99L));
        verifyNoInteractions(events);
    }

    @Test
    void getThrowsWhenCourseIsUnknown() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> service.get(99L));
    }

    private void stubExistingCourse(long id) {
        Course course = Course.create("Java", 10, "Berlin");
        ReflectionTestUtils.setField(course, "id", id);
        when(repository.findById(id)).thenReturn(Optional.of(course));
        when(repository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
