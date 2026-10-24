package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainTrip2Repository extends JpaRepository<TrainTrip2, Long> {

	public TrainTrip2 findFirstByOrderByStartDesc();
	
	public List<TrainTrip2> findByLine(TrainLine line);

	public List<TrainTrip2> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
	public List<TrainTrip2> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(t.start) FROM TrainTrip2 t")
    OffsetDateTime findMinDate();
    @Query("SELECT MAX(t.start) FROM TrainTrip2 t")
    OffsetDateTime findMaxDate();
}
