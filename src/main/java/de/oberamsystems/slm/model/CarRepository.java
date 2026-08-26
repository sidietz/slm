package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
	
	public Car findFirstByOrderByApprovalDateDesc();
	public List<Car> findAllByOrderByApprovalDateDesc();
}
