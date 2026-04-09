CREATE TABLE course (
    course_code VARCHAR(50) PRIMARY KEY,
    capacity INTEGER NOT NULL,
    enrolled_count INTEGER NOT NULL DEFAULT 0
);