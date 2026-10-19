package de.oberamsystems.slm.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureNoteRepository extends JpaRepository<LectureNote, UUID> {

	List<LectureNote> findByCourseIdOrderByLectureNumberAsc(Long courseId);

	List<LectureNote> findByCourseIdOrderByLectureDateDesc(Long courseId);

	List<LectureNote> findAllByOrderByCreatedAtDesc();

	Optional<LectureNote> findByLectureSessionId(UUID lectureSessionId);

	@Query("SELECT n FROM LectureNote n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.contentMarkdown) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.keyConcepts) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY n.createdAt DESC")
	List<LectureNote> searchNotes(@Param("query") String query);
}
