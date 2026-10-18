package de.oberamsystems.slm.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureSessionRepository extends JpaRepository<LectureSession, String> {

	List<LectureSession> findByCourseIdOrderByLectureNumberAsc(Long courseId);

	List<LectureSession> findByCourseIdOrderByStartDesc(Long courseId);
}
