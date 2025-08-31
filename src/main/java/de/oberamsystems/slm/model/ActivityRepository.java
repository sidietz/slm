package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

	public Activity findFirstByOrderByStartDesc();
	
	public List<Activity> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);
	public List<Activity> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(a.start) FROM Activity a")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(a.start) FROM Activity a")
    LocalDateTime findMaxDate();
}
