package com.trainingplatform.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trainingplatform.catalog.api.CoursePublishedEvent;
import com.trainingplatform.enrollments.api.EnrollmentCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@RecordApplicationEvents
@ExtendWith(OutputCaptureExtension.class)
class EnrollmentsVisitorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void visitorEnrollsInPublishedCourse(CapturedOutput output) throws Exception {
        long courseId = createCourse(5);
        publish(courseId);

        assertTrue(applicationEvents.stream(CoursePublishedEvent.class)
                .anyMatch(event -> event.equals(new CoursePublishedEvent(courseId))));
        assertTrue(output.getOut().contains("Course " + courseId + " is open for enrollment"));

        mockMvc.perform(enroll("Ann", courseId))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Ann"))
                .andExpect(jsonPath("$.courseId").value(courseId));

        mockMvc.perform(get("/api/v1/enrollments").param("courseId", String.valueOf(courseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("ann@x.com"));

        assertEquals(1, applicationEvents.stream(EnrollmentCreatedEvent.class)
                .filter(event -> event.courseId() == courseId)
                .count());
    }

    @Test
    void unpublishedCourseIsRejected() throws Exception {
        long courseId = createCourse(5);

        mockMvc.perform(enroll("Ann", courseId))
                .andExpect(status().isConflict());

        assertEquals(0, applicationEvents.stream(EnrollmentCreatedEvent.class)
                .filter(event -> event.courseId() == courseId)
                .count());
    }

    @Test
    void fullCourseIsRejected() throws Exception {
        long courseId = createCourse(1);
        publish(courseId);

        mockMvc.perform(enroll("Ann", courseId)).andExpect(status().isCreated());
        mockMvc.perform(enroll("Bob", courseId)).andExpect(status().isConflict());

        assertEquals(1, applicationEvents.stream(EnrollmentCreatedEvent.class)
                .filter(event -> event.courseId() == courseId)
                .count());
    }

    @Test
    void unknownCourseReturnsNotFound() throws Exception {
        mockMvc.perform(enroll("Ann", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void blankNameIsRejected() throws Exception {
        long courseId = createCourse(5);
        publish(courseId);

        mockMvc.perform(enroll(" ", courseId))
                .andExpect(status().isBadRequest());
    }

    private long createCourse(int capacity) throws Exception {
        String location = mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\",\"capacity\":" + capacity + ",\"city\":\"Berlin\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    private void publish(long courseId) throws Exception {
        mockMvc.perform(post("/api/v1/courses/" + courseId + "/publish"))
                .andExpect(status().isOk());
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder enroll(
            String name, long courseId) {
        return post("/api/v1/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"email\":\"ann@x.com\",\"courseId\":" + courseId + "}");
    }
}
