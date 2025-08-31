package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainTripRepository extends JpaRepository<TrainTrip, Long> {

	public TrainTrip findFirstByOrderByStartDesc();
	
	public List<TrainTrip> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);
	public List<TrainTrip> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(t.start) FROM TrainTrip t")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(t.start) FROM TrainTrip t")
    LocalDateTime findMaxDate();
}
