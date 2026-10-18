package de.oberamsystems.slm.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	@Query(value = "SELECT ROUND((ST_Distance(p1.geom, p2.geom) / 1000.0)::numeric, 2) " +
	               "FROM point p1, point p2 WHERE p1.id = :originId AND p2.id = :practiceId", nativeQuery = true)
	Double calculateAirDistance(@Param("originId") Long originId, @Param("practiceId") Long practiceId);
}
