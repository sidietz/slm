package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SportSessionRepository extends JpaRepository<SportSession, UUID> {

	public SportSession findFirstByOrderByStartDesc();

	public List<SportSession> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

	public List<SportSession> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);

	@Query("SELECT MIN(s.start) FROM SportSession s")
	OffsetDateTime findMinDate();

	@Query("SELECT MAX(s.start) FROM SportSession s")
	OffsetDateTime findMaxDate();
}
