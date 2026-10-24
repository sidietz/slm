package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	public Appointment findFirstByOrderByStartDesc();
	
	public List<Appointment> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
	public List<Appointment> findByStartBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(a.start) FROM Appointment a")
    OffsetDateTime findMinDate();
    @Query("SELECT MAX(a.start) FROM Appointment a")
    OffsetDateTime findMaxDate();
}
