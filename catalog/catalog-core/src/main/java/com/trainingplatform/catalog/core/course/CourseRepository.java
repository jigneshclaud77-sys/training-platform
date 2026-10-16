package com.trainingplatform.catalog.core.course;

import org.springframework.data.jpa.repository.JpaRepository;

interface CourseRepository extends JpaRepository<Course, Long> {
}
