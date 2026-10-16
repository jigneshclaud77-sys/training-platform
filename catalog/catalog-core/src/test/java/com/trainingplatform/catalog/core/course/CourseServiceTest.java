package com.trainingplatform.catalog.core.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.trainingplatform.catalog.api.CourseDto;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CourseServiceTest {

    private final CourseRepository repository = mock(CourseRepository.class);
    private final CourseService service = new CourseService(repository);

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
    void getThrowsWhenCourseIsUnknown() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> service.get(99L));
    }
}
