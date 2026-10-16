package com.trainingplatform.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trainingplatform.catalog.api.CourseDto;
import com.trainingplatform.catalog.api.CoursePublishedEvent;
import com.trainingplatform.catalog.api.CoursesApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@RecordApplicationEvents
class CatalogCreateCourseIntegrationTest {

    private static final String COURSE_JSON = "{\"title\":\"Java\",\"capacity\":10,\"city\":\"Berlin\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CoursesApi coursesApi;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void createsUnpublishedCourseAndReadsItBack() throws Exception {
        String location = mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COURSE_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Java"))
                .andExpect(jsonPath("$.capacity").value(10))
                .andExpect(jsonPath("$.city").value("Berlin"))
                .andExpect(jsonPath("$.published").value(false))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java"))
                .andExpect(jsonPath("$.published").value(false));

        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
        CourseDto dto = coursesApi.findCourse(id);
        assertEquals(new CourseDto(id, "Java", 10, "Berlin", false), dto);
    }

    @Test
    void publishesCourseAndEmitsEvent() throws Exception {
        String location = mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COURSE_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(post(location + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        mockMvc.perform(post(location + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        assertTrue(coursesApi.findCourse(id).published());
        assertTrue(applicationEvents.stream(CoursePublishedEvent.class)
                .anyMatch(event -> event.equals(new CoursePublishedEvent(id))));
    }

    @Test
    void publishingUnknownCourseReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/courses/999999/publish"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownCourseReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/courses/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void blankTitleIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"capacity\":10,\"city\":\"Berlin\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonPositiveCapacityIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\",\"capacity\":0,\"city\":\"Berlin\"}"))
                .andExpect(status().isBadRequest());
    }
}
