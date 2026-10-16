package com.trainingplatform.enrollments.core.enrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollments")
class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id")
    private long courseId;

    private String name;
    private String email;

    protected Enrollment() {
    }

    private Enrollment(long courseId, String name, String email) {
        this.courseId = courseId;
        this.name = name;
        this.email = email;
    }

    static Enrollment create(long courseId, String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Enrollment name must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Enrollment email must not be blank");
        }
        return new Enrollment(courseId, name.trim(), email.trim());
    }

    Long getId() {
        return id;
    }

    long getCourseId() {
        return courseId;
    }

    String getName() {
        return name;
    }

    String getEmail() {
        return email;
    }
}
