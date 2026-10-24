package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SleepSessionRepository extends JpaRepository<SleepSession, UUID> {

	public SleepSession findFirstByOrderByStartDesc();

	List<SleepSession> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

	List<SleepSession> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);

	@Query("SELECT MIN(s.start) FROM SleepSession s")
	OffsetDateTime findMinDate();
}
