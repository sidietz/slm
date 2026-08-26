package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CarTripRepository extends JpaRepository<CarTrip, Long> {
	
	public CarTrip findFirstByOrderByStartDesc();
	public List<CarTrip> findAllByOrderByStart();
}
