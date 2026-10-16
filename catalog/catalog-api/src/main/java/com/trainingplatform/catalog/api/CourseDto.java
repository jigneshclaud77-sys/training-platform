package com.trainingplatform.catalog.api;

public record CourseDto(
        long id,
        String title,
        int capacity,
        String city,
        boolean published
) {}
