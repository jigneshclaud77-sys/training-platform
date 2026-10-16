package com.trainingplatform.enrollments.core.course;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.trainingplatform.catalog.api.CoursePublishedEvent;
import org.junit.jupiter.api.Test;

class CoursePublishedHandlerTest {

    @Test
    void handlesCoursePublishedEvent() {
        assertDoesNotThrow(() -> new CoursePublishedHandler().on(new CoursePublishedEvent(1L)));
    }
}
