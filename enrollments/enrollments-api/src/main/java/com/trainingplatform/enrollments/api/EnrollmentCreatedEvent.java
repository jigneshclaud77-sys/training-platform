package com.trainingplatform.enrollments.api;

public record EnrollmentCreatedEvent(
        long enrollmentId,
        long courseId,
        String name,
        String email
) {}
