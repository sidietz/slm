package de.oberamsystems.slm;

import de.oberamsystems.slm.model.Point;
import de.oberamsystems.slm.model.PointRepository;
import de.oberamsystems.slm.model.Route;
import de.oberamsystems.slm.model.RouteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class RouteSpatialIT extends AbstractIntegrationTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    public void testPostgisDistanceCalculationAndTriggers() {
        // Frankfurt am Main coordinates
        Point p1 = new Point();
        p1.setName("Frankfurt Central");
        p1.setDescription("Frankfurt Hbf");
        p1.setLatitude(50.1071);
        p1.setLongitude(8.6638);
        p1 = pointRepository.saveAndFlush(p1);

        // Munich Central coordinates
        Point p2 = new Point();
        p2.setName("Munich Central");
        p2.setDescription("München Hbf");
        p2.setLatitude(48.1402);
        p2.setLongitude(11.5583);
        p2 = pointRepository.saveAndFlush(p2);

        // Verify native PostGIS ST_Distance calculation (~304 km between Frankfurt and Munich)
        Double airDistance = routeRepository.calculateAirDistance(p1.getId(), p2.getId());
        assertNotNull(airDistance, "Calculated air distance must not be null");
        assertTrue(airDistance >= 300.0 && airDistance <= 315.0, 
                "Air distance between Frankfurt and Munich should be around 304 km, got: " + airDistance);

        // Test route trigger: air_distance should be calculated automatically on insert
        Route route = new Route();
        route.setName("Frankfurt - Munich Express");
        route.setDescription("Direct Highway Route");
        route.setStart(p1);
        route.setEnd(p2);
        route.setDriveDistance(390.0);
        route = routeRepository.saveAndFlush(route);
        entityManager.refresh(route);

        assertNotNull(route.getAirDistance(), "Route trigger must calculate air_distance on insert");
        assertEquals(airDistance, route.getAirDistance(), 0.1);
    }
}
