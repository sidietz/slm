package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<Session, UUID> {

	List<Session> findByStatus(String status);

	List<Session> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);

	List<Session> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);

	@Query("SELECT MIN(s.start) FROM Session s")
	LocalDateTime findMinDate();

	@Query("SELECT MAX(s.start) FROM Session s")
	LocalDateTime findMaxDate();
}
