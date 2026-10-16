package com.trainingplatform.catalog.core.course;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int capacity;
    private String city;
    private boolean published;

    protected Course() {
    }

    private Course(String title, int capacity, String city) {
        this.title = title;
        this.capacity = capacity;
        this.city = city;
        this.published = false;
    }

    static Course create(String title, int capacity, String city) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title must not be blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Course city must not be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Course capacity must be greater than 0");
        }
        return new Course(title.trim(), capacity, city.trim());
    }

    void publish() {
        this.published = true;
    }

    Long getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    int getCapacity() {
        return capacity;
    }

    String getCity() {
        return city;
    }

    boolean isPublished() {
        return published;
    }
}
