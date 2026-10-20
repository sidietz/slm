package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargingPointRepository extends JpaRepository<ChargingPoint, UUID> {

	List<ChargingPoint> findByChargingStationIdOrderByMaxPowerKwDesc(UUID chargingStationId);

	List<ChargingPoint> findByPlugType(PlugType plugType);
}
