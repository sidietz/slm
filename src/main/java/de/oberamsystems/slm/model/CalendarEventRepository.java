package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

	public CalendarEvent findFirstByOrderByStartDesc();

	List<CalendarEvent> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
	List<CalendarEvent> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);

	List<CalendarEvent> findByStartAfter(OffsetDateTime ldt);
	List<CalendarEvent> findByStartAfter(OffsetDateTime ldt, Sort sort);
	
	@Query("SELECT MIN(s.start) FROM CalendarEvent s")
    OffsetDateTime findMinDate();
    @Query("SELECT MAX(s.start) FROM CalendarEvent s")
    OffsetDateTime findMaxDate();
}
