package de.oberamsystems.slm.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DroneRepository extends JpaRepository<Drone, UUID> {

    List<Drone> findAllByOrderByNameAsc();

    List<Drone> findByCategoryOrderByNameAsc(DroneCategory category);

    List<Drone> findByActiveTrueOrderByNameAsc();
}
