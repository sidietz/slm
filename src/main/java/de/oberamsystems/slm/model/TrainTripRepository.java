package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainTripRepository extends JpaRepository<TrainTrip, Long> {

	public TrainTrip findFirstByOrderByStartDesc();
	
	public List<TrainTrip> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
	public List<TrainTrip> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(t.start) FROM TrainTrip t")
    OffsetDateTime findMinDate();
    @Query("SELECT MAX(t.start) FROM TrainTrip t")
    OffsetDateTime findMaxDate();
}
