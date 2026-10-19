package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefuelingRepository extends JpaRepository<Refueling, UUID> {

	List<Refueling> findAllByOrderByFuelingDateDesc();

	List<Refueling> findByCarIdOrderByFuelingDateDesc(Long carId);

	List<Refueling> findAllByOrderByFuelingDateAsc();

	List<Refueling> findByCarIdOrderByFuelingDateAsc(Long carId);

	List<Refueling> findByStationIdOrderByFuelingDateDesc(UUID stationId);
}
