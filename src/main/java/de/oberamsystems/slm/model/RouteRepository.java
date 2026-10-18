package de.oberamsystems.slm.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteRepository extends JpaRepository<Route, Long> {

	@Query(value = "SELECT ROUND((ST_Distance(p1.geom, p2.geom) / 1000.0)::numeric, 2) " +
	               "FROM point p1, point p2 WHERE p1.id = :startId AND p2.id = :endId", nativeQuery = true)
	Double calculateAirDistance(@Param("startId") Long startId, @Param("endId") Long endId);
}
