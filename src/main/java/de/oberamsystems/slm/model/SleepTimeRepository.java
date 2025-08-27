package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;

public interface SleepTimeRepository extends JpaRepository<SleepTime, Long> {
	public List<SleepTime> findByGotobedBetween(LocalDateTime fromDate, LocalDateTime toDate);
	public List<SleepTime> findByGotobedBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(t.gotobed) FROM SleepTime t")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(t.getup) FROM SleepTime t")
    LocalDateTime findMaxDate();
}
