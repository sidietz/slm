package de.oberamsystems.slm;

import de.oberamsystems.slm.model.Doctor;
import de.oberamsystems.slm.model.DoctorRepository;
import de.oberamsystems.slm.model.Point;
import de.oberamsystems.slm.model.PointRepository;
import de.oberamsystems.slm.model.Speciality;
import de.oberamsystems.slm.model.SpecialityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class DoctorSpatialIT extends AbstractIntegrationTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private SpecialityRepository specialityRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    public void testDoctorAirDistanceCalculationAndTrigger() {
        // Home base point
        Point home = new Point();
        home.setName("Home Base");
        home.setDescription("Home location");
        home.setLatitude(49.2770);
        home.setLongitude(10.6850);
        home.setIsHome(true);
        home = pointRepository.saveAndFlush(home);

        // Clinic point in Ansbach (~11-12 km away)
        Point clinic = new Point();
        clinic.setName("Ansbach Practice");
        clinic.setDescription("Draisstraße 2, Ansbach");
        clinic.setLatitude(49.3087);
        clinic.setLongitude(10.5847);
        clinic = pointRepository.saveAndFlush(clinic);

        Speciality spec = new Speciality();
        spec.setTitle("Allgemeinmedizin");
        spec = specialityRepository.saveAndFlush(spec);

        // Native calculation query
        Double calculated = doctorRepository.calculateAirDistance(home.getId(), clinic.getId());
        assertNotNull(calculated);
        assertTrue(calculated >= 7.0 && calculated <= 15.0, 
                "Air distance should be ~7-15 km, got: " + calculated);

        // Insert Doctor with clinic point and base point
        Doctor doctor = new Doctor();
        doctor.setFirstname("Ulrich");
        doctor.setLastname("Wilms");
        doctor.setSpeciality(spec);
        doctor.setCity("Ansbach");
        doctor.setPoint(clinic);
        doctor.setBasePoint(home);
        doctor = doctorRepository.saveAndFlush(doctor);
        entityManager.refresh(doctor);

        assertNotNull(doctor.getAirDistance(), "Doctor trigger must calculate air_distance on insert");
        assertEquals(calculated, doctor.getAirDistance(), 0.1);
    }
}
