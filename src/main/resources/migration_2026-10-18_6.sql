-- Migration 2026-10-18_6: University Courses, Lecture Sessions & Lecture Notes Architecture

-- 1. Create course table
CREATE TABLE IF NOT EXISTS course (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    code TEXT,
    semester TEXT,
    ects_credits NUMERIC(3, 1),
    lecturer_id BIGINT REFERENCES human(id) ON DELETE SET NULL,
    default_room TEXT,
    meeting_link TEXT,
    syllabus TEXT,
    exam_date TIMESTAMP,
    exam_location TEXT,
    grade NUMERIC(3, 2),
    exam_passed BOOLEAN,
    status status_type DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP,
    last_updated TIMESTAMP
);

-- 2. Create lecture_session joined table
CREATE TABLE IF NOT EXISTS lecture_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    course_id BIGINT REFERENCES course(id) ON DELETE SET NULL,
    lecture_number INTEGER,
    topic TEXT,
    room TEXT,
    recording_url TEXT,
    slides_url TEXT
);

-- 3. Create lecture_note table with UUIDv7
CREATE TABLE IF NOT EXISTS lecture_note (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    course_id BIGINT REFERENCES course(id) ON DELETE CASCADE,
    lecture_session_id TEXT REFERENCES lecture_session(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    lecture_number INTEGER,
    lecture_date DATE,
    content_markdown TEXT,
    key_concepts TEXT,
    summary TEXT,
    has_homework BOOLEAN DEFAULT FALSE,
    homework_todo TEXT,
    exam_relevance_rating INTEGER,
    created_at TIMESTAMP,
    last_updated TIMESTAMP
);

-- 4. Create indices for foreign keys and queries
CREATE INDEX IF NOT EXISTS idx_course_lecturer ON course(lecturer_id);
CREATE INDEX IF NOT EXISTS idx_course_semester ON course(semester);
CREATE INDEX IF NOT EXISTS idx_lecture_session_course ON lecture_session(course_id);
CREATE INDEX IF NOT EXISTS idx_lecture_note_course ON lecture_note(course_id);
CREATE INDEX IF NOT EXISTS idx_lecture_note_session ON lecture_note(lecture_session_id);
