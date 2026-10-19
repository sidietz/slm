package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, UUID> {

	List<ChargingStation> findAllByOrderByNameAsc();

	List<ChargingStation> findByOperatorIgnoreCase(String operator);
}
