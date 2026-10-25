package de.oberamsystems.slm.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DroneFlightRepository extends JpaRepository<DroneFlight, UUID> {

    List<DroneFlight> findAllByOrderByStartDesc();

    List<DroneFlight> findByDroneIdOrderByStartDesc(UUID droneId);

    List<DroneFlight> findByDroneOrderByStartDesc(Drone drone);

    List<DroneFlight> findByStartBetweenOrderByStartDesc(OffsetDateTime from, OffsetDateTime to);

    @Query("SELECT COUNT(df) FROM DroneFlight df WHERE df.drone.id = :droneId")
    long countByDroneId(@Param("droneId") UUID droneId);

    @Query("SELECT COALESCE(SUM(df.airDistance), 0.0) FROM DroneFlight df WHERE df.drone.id = :droneId")
    Double sumAirDistanceByDroneId(@Param("droneId") UUID droneId);
}
