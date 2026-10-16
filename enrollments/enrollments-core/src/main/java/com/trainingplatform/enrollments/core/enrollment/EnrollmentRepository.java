package com.trainingplatform.enrollments.core.enrollment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByCourseId(long courseId);

    long countByCourseId(long courseId);
}
