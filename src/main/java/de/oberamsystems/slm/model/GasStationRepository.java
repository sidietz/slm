package de.oberamsystems.slm.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GasStationRepository extends JpaRepository<GasStation, UUID> {

	List<GasStation> findAllByOrderByNameAsc();

	Optional<GasStation> findByNameIgnoreCaseAndCityIgnoreCase(String name, String city);

	Optional<GasStation> findByNameIgnoreCase(String name);

	Optional<GasStation> findByTankentankenId(UUID tankentankenId);
}
