package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SportSessionRepository extends JpaRepository<SportSession, Long> {

	public SportSession findFirstByOrderByStartDesc();
	
	public List<SportSession> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);
	public List<SportSession> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(s.start) FROM SportSession s")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(s.start) FROM SportSession s")
    LocalDateTime findMaxDate();
}
