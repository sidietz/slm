package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainTrip2Repository extends JpaRepository<TrainTrip2, Long> {

	public TrainTrip2 findFirstByOrderByStartDesc();
	
	public List<TrainTrip2> findByLine(TrainLine line);

	public List<TrainTrip2> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);
	public List<TrainTrip2> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(t.start) FROM TrainTrip2 t")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(t.start) FROM TrainTrip2 t")
    LocalDateTime findMaxDate();
}
