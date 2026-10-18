package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseRepository extends JpaRepository<Course, Long> {

	List<Course> findAllByOrderByCreatedAtDesc();

	List<Course> findAllBySemesterOrderByTitleAsc(String semester);

	List<Course> findByStatus(StatusEnum status);

	@Query("SELECT DISTINCT c.semester FROM Course c WHERE c.semester IS NOT NULL AND c.semester <> '' ORDER BY c.semester DESC")
	List<String> findDistinctSemesters();

	List<Course> findByExamDateBetweenOrderByExamDateAsc(LocalDateTime start, LocalDateTime end);

	List<Course> findByLecturerId(Long lecturerId);
}
