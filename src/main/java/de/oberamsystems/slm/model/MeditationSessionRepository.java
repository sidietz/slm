package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeditationSessionRepository extends JpaRepository<MeditationSession, UUID> {

	public MeditationSession findFirstByOrderByStartDesc();

	List<MeditationSession> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

	List<MeditationSession> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);

	@Query("SELECT MIN(s.start) FROM MeditationSession s")
	OffsetDateTime findMinDate();

	@Query("SELECT MAX(s.start) FROM MeditationSession s")
	OffsetDateTime findMaxDate();
}
