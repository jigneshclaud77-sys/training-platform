CREATE TABLE enrollments (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT       NOT NULL,
    name      VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL
);

CREATE INDEX idx_enrollments_course_id ON enrollments (course_id);
