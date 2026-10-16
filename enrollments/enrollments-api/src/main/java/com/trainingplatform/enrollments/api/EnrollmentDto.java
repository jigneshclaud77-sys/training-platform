package com.trainingplatform.enrollments.api;

public record EnrollmentDto(
        long id,
        String name,
        String email,
        long courseId
) {}
