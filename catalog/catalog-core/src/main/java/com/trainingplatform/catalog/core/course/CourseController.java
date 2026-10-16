package com.trainingplatform.catalog.core.course;

import com.trainingplatform.catalog.api.CourseDto;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses")
class CourseController {

    private final CourseService service;

    CourseController(CourseService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<CourseDto> create(@RequestBody CreateCourseRequest request) {
        CourseDto course = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/courses/" + course.id())).body(course);
    }

    @GetMapping("/{id}")
    CourseDto get(@PathVariable long id) {
        return service.get(id);
    }
}
