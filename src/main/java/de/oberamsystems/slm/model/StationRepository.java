package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Station, UUID> {

	List<Station> findAllByOrderByNameAsc();

	List<Station> findByStationTypeOrderByNameAsc(StationType stationType);

	List<Station> findByCountryOrderByNameAsc(String country);
}
