CREATE TABLE students (
    id UUID PRIMARY KEY,
    student_no VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL
);

CREATE TABLE courses (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    enrolled_count INT NOT NULL
);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_entries (
    id UUID PRIMARY KEY,
    event_type VARCHAR(128) NOT NULL,
    entity_type VARCHAR(128) NOT NULL,
    entity_id UUID NOT NULL,
    payload TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_audit_created_at ON audit_entries(created_at DESC);

INSERT INTO courses (id, code, name, capacity, enrolled_count) VALUES
    ('11111111-1111-1111-1111-111111111111', 'CSE401', 'Distributed Systems', 2, 0),
    ('22222222-2222-2222-2222-222222222222', 'CSE402', 'Cloud Engineering', 1, 0),
    ('33333333-3333-3333-3333-333333333333', 'CSE403', 'Event-Driven Architecture', 3, 0);
