package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HumanEventRepository extends JpaRepository<HumanEvent, Long> {

	List<HumanEvent> findAllByOrderByDaysUntilNextAsc();

	List<HumanEvent> findByEventTypeOrderByDaysUntilNextAsc(EventType eventType);

	List<HumanEvent> findByHumanIdOrderByDaysUntilNextAsc(Long humanId);

	HumanEvent findFirstByOrderByDaysUntilNextAsc();

	@Query("SELECT e FROM HumanEvent e WHERE e.daysUntilNext <= :daysLimit ORDER BY e.daysUntilNext ASC")
	List<HumanEvent> findUpcomingEventsWithinDays(@Param("daysLimit") int daysLimit);
}
