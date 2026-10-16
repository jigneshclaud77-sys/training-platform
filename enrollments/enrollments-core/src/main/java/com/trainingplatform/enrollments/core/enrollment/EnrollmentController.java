package com.trainingplatform.enrollments.core.enrollment;

import com.trainingplatform.enrollments.api.EnrollmentDto;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enrollments")
class EnrollmentController {

    private final EnrollmentService service;

    EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<EnrollmentDto> enroll(@RequestBody CreateEnrollmentRequest request) {
        EnrollmentDto enrollment = service.enroll(request);
        return ResponseEntity.created(URI.create("/api/v1/enrollments/" + enrollment.id()))
                .body(enrollment);
    }

    @GetMapping
    List<EnrollmentDto> list(@RequestParam long courseId) {
        return service.listByCourse(courseId);
    }
}
