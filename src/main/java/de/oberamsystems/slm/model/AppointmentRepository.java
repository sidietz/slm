package de.oberamsystems.slm.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

public Appointment findFirstByOrderByStartDesc();
	
	public List<Appointment> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate);
	public List<Appointment> findByStartBetween(LocalDateTime fromDate, LocalDateTime toDate, Sort sort);
	
    @Query("SELECT MIN(a.start) FROM Appointment a")
    LocalDateTime findMinDate();
    @Query("SELECT MAX(a.start) FROM Appointment a")
    LocalDateTime findMaxDate();
}
