package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureSessionRepository extends JpaRepository<LectureSession, UUID> {

	List<LectureSession> findByCourseIdOrderByLectureNumberAsc(Long courseId);

	List<LectureSession> findByCourseIdOrderByStartDesc(Long courseId);
}
