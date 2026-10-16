CREATE TABLE courses (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    capacity  INT          NOT NULL CHECK (capacity > 0),
    city      VARCHAR(255) NOT NULL,
    published BOOLEAN      NOT NULL DEFAULT FALSE
);
