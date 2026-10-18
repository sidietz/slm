package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SleepSessionRepository extends JpaRepository<SleepSession, String> {

	public SleepSession findFirstByOrderByStartDesc();

	List<SleepSession> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);

	List<SleepSession> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);

	@Query("SELECT MIN(s.start) FROM SleepSession s")
	LocalDateTime findMinDate();
}
