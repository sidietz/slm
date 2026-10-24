package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<Session, UUID> {

	List<Session> findByStatus(String status);

	List<Session> findByStatusOrderByStartDesc(String status);

	List<Session> findByEndIsNullOrderByStartDesc();

	Optional<Session> findFirstByEndIsNullOrderByStartDesc();

	List<Session> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

	List<Session> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);

	@Query("SELECT MIN(s.start) FROM Session s")
	OffsetDateTime findMinDate();

	@Query("SELECT MAX(s.start) FROM Session s")
	OffsetDateTime findMaxDate();
}
