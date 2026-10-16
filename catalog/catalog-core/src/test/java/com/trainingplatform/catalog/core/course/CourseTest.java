package com.trainingplatform.catalog.core.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CourseTest {

    @Test
    void newCourseIsUnpublished() {
        Course course = Course.create("Java", 10, "Berlin");

        assertEquals("Java", course.getTitle());
        assertEquals(10, course.getCapacity());
        assertEquals("Berlin", course.getCity());
        assertFalse(course.isPublished());
    }

    @Test
    void publishMarksCourseAsPublished() {
        Course course = Course.create("Java", 10, "Berlin");

        course.publish();

        assertTrue(course.isPublished());
    }

    @Test
    void publishIsIdempotent() {
        Course course = Course.create("Java", 10, "Berlin");

        course.publish();
        course.publish();

        assertTrue(course.isPublished());
    }

    @Test
    void rejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> Course.create(" ", 10, "Berlin"));
        assertThrows(IllegalArgumentException.class, () -> Course.create(null, 10, "Berlin"));
    }

    @Test
    void rejectsBlankCity() {
        assertThrows(IllegalArgumentException.class, () -> Course.create("Java", 10, ""));
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> Course.create("Java", 0, "Berlin"));
        assertThrows(IllegalArgumentException.class, () -> Course.create("Java", -1, "Berlin"));
    }
}
