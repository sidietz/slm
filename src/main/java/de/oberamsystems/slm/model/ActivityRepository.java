package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

	public Activity findFirstByOrderByStartDesc();
	
	public List<Activity> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
	public List<Activity> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(a.start) FROM Activity a")
    OffsetDateTime findMinDate();
    @Query("SELECT MAX(a.start) FROM Activity a")
    OffsetDateTime findMaxDate();
}
