package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeditationSessionRepository extends JpaRepository<MeditationSession, Long> {
	
	List<MeditationSession> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);
	List<MeditationSession> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);

	@Query("SELECT MIN(s.start) FROM MeditationSession s")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(s.start) FROM MeditationSession s")
    LocalDateTime findMaxDate();
}
