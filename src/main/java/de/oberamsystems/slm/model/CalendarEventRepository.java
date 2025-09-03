package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

	public CalendarEvent findFirstByOrderByStartDesc();

	List<CalendarEvent> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);
	List<CalendarEvent> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);

	List<CalendarEvent> findByStartAfter(LocalDateTime ldt);
	List<CalendarEvent> findByStartAfter(LocalDateTime ldt, Sort sort);
	
	@Query("SELECT MIN(s.start) FROM CalendarEvent s")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(s.start) FROM CalendarEvent s")
    LocalDateTime findMaxDate();
}
