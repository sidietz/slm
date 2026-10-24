package de.oberamsystems.slm;

import de.oberamsystems.slm.config.TimeZoneConfig;
import de.oberamsystems.slm.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class DateTimePrecisionIT extends AbstractIntegrationTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SleepTimeRepository sleepTimeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialityRepository specialityRepository;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RefuelingRepository refuelingRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testSpringDaylightSavingTimeTransitionDuration() {
        // In Europe/Berlin, on Sunday March 29, 2026, clocks turn forward 1 hour at 02:00 to 03:00.
        // A session spanning from 23:00 CET (2026-03-28) to 07:00 CEST (2026-03-29) is physically 7 hours long.
        OffsetDateTime start = toOffset(2026, 3, 28, 23, 0);
        OffsetDateTime end = toOffset(2026, 3, 29, 7, 0);

        CodingSession session = new CodingSession();
        session.setStart(start);
        session.setEnd(end);
        session.setProject("SLM-DST");
        session.setLanguage("Java");
        session.setBreakDuration(Duration.ZERO);

        session = sessionRepository.saveAndFlush(session);
        entityManager.refresh(session);

        assertNotNull(session.getId());
        assertEquals(Duration.ofHours(7), session.getDuration(),
                "PostgreSQL TIMESTAMPTZ interval subtraction across spring DST jump (+1h) must yield 7 hours");
        assertEquals(Duration.ofHours(7), session.getNetDuration(),
                "Net duration must equal gross duration when break is zero");
    }

    @Test
    public void testFallDaylightSavingTimeTransitionDuration() {
        // In Europe/Berlin, on Sunday October 25, 2026, clocks turn back 1 hour at 03:00 to 02:00.
        // A session spanning from 23:00 CEST (2026-10-24) to 07:00 CET (2026-10-25) is physically 9 hours long.
        OffsetDateTime start = toOffset(2026, 10, 24, 23, 0);
        OffsetDateTime end = toOffset(2026, 10, 25, 7, 0);

        CodingSession session = new CodingSession();
        session.setStart(start);
        session.setEnd(end);
        session.setProject("SLM-DST-FALL");
        session.setLanguage("Java");
        session.setBreakDuration(Duration.ZERO);

        session = sessionRepository.saveAndFlush(session);
        entityManager.refresh(session);

        assertNotNull(session.getId());
        assertEquals(Duration.ofHours(9), session.getDuration(),
                "PostgreSQL TIMESTAMPTZ interval subtraction across fall DST jump (-1h) must yield 9 hours");
        assertEquals(Duration.ofHours(9), session.getNetDuration(),
                "Net duration must equal gross duration when break is zero");
    }

    @Test
    public void testSleepTimeDstTransitionDurations() {
        // Spring DST transition in SleepTime table (gotobed -> standup)
        SleepTime springSleep = new SleepTime();
        springSleep.setGotobed(toOffset(2026, 3, 28, 23, 0));
        springSleep.setGetup(toOffset(2026, 3, 29, 7, 0));

        springSleep = sleepTimeRepository.saveAndFlush(springSleep);
        entityManager.refresh(springSleep);

        assertEquals(Duration.ofHours(7), springSleep.getSleeptime(),
                "SleepTime across spring DST must be 7 hours");

        // Fall DST transition in SleepTime table
        SleepTime fallSleep = new SleepTime();
        fallSleep.setGotobed(toOffset(2026, 10, 24, 23, 0));
        fallSleep.setGetup(toOffset(2026, 10, 25, 7, 0));

        fallSleep = sleepTimeRepository.saveAndFlush(fallSleep);
        entityManager.refresh(fallSleep);

        assertEquals(Duration.ofHours(9), fallSleep.getSleeptime(),
                "SleepTime across fall DST must be 9 hours");
    }

    @Test
    public void testAppointmentDurationCalculationWithTimestamptz() {
        Speciality spec = new Speciality(null, "General Medicine");
        spec = specialityRepository.saveAndFlush(spec);

        Doctor doctor = new Doctor();
        doctor.setFirstname("Max");
        doctor.setLastname("Mustermann");
        doctor.setSpeciality(spec);
        doctor = doctorRepository.saveAndFlush(doctor);

        OffsetDateTime departure = toOffset(2026, 6, 15, 8, 30);
        OffsetDateTime arrival = toOffset(2026, 6, 15, 9, 0);
        OffsetDateTime start = toOffset(2026, 6, 15, 9, 15);
        OffsetDateTime end = toOffset(2026, 6, 15, 10, 0);
        OffsetDateTime arrivalHome = toOffset(2026, 6, 15, 10, 30);

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setDeparture(departure);
        appointment.setArrival(arrival);
        appointment.setStart(start);
        appointment.setEnd(end);
        appointment.setArrivalHome(arrivalHome);
        appointment.setComment("Routine Checkup");

        appointment = appointmentRepository.saveAndFlush(appointment);
        entityManager.refresh(appointment);

        assertEquals(Duration.ofMinutes(30), appointment.getTravelDuration());
        assertEquals(Duration.ofMinutes(45), appointment.getDuration());
        assertEquals(Duration.ofHours(2), appointment.getCompleteDuration());
    }

    @Test
    public void testCalendarEventDurationAndWallClockPreservation() {
        OffsetDateTime start = toOffset(2026, 5, 10, 14, 0);
        OffsetDateTime end = toOffset(2026, 5, 10, 16, 30);

        CalendarEvent event = new CalendarEvent();
        event.setTitle("Tech Architecture Review");
        event.setDescription("Discussing TIMESTAMPTZ migration");
        event.setStart(start);
        event.setEnd(end);

        event = calendarEventRepository.saveAndFlush(event);
        Long id = event.getId();

        entityManager.clear();

        CalendarEvent reloaded = calendarEventRepository.findById(id).orElseThrow();
        assertTrue(start.isEqual(reloaded.getStart()), "Start time must preserve exact instant");
        assertTrue(end.isEqual(reloaded.getEnd()), "End time must preserve exact instant");
        assertEquals(Duration.ofMinutes(150), reloaded.getDuration(), "Duration must be 2h 30m");
    }

    @Test
    public void testCourseAndRefuelingTimestamptzPersistence() {
        OffsetDateTime examDate = toOffset(2026, 7, 20, 10, 0);
        Course course = new Course();
        course.setTitle("Distributed Systems");
        course.setCode("CS-401");
        course.setSemester("Summer 2026");
        course.setExamDate(examDate);
        course = courseRepository.saveAndFlush(course);

        Long courseId = course.getId();
        entityManager.clear();

        Course reloadedCourse = courseRepository.findById(courseId).orElseThrow();
        assertTrue(examDate.isEqual(reloadedCourse.getExamDate()));
        assertNotNull(reloadedCourse.getCreatedAt(), "CreatedAt must be auto-populated");
        assertNotNull(reloadedCourse.getLastUpdated(), "LastUpdated must be auto-populated");

        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName("Volkswagen AG");
        manufacturer = manufacturerRepository.saveAndFlush(manufacturer);

        Vendor vendor = new Vendor();
        vendor.setName("Autohaus München");
        vendor = vendorRepository.saveAndFlush(vendor);

        Car car = new Car();
        car.setName("Test Auto DST");
        car.setModelName("Golf");
        car.setManufacturer(manufacturer);
        car.setVendor(vendor);
        car = carRepository.saveAndFlush(car);

        OffsetDateTime fuelingDate = toOffset(2026, 8, 1, 12, 45);
        Refueling refueling = new Refueling();
        refueling.setCar(car);
        refueling.setFuelingDate(fuelingDate);
        refueling.setLiters(new BigDecimal("45.50"));
        refueling.setTotalCost(new BigDecimal("79.99"));
        refueling = refuelingRepository.saveAndFlush(refueling);

        UUID refuelingId = refueling.getId();
        entityManager.clear();

        Refueling reloadedRefueling = refuelingRepository.findById(refuelingId).orElseThrow();
        assertTrue(fuelingDate.isEqual(reloadedRefueling.getFuelingDate()));
        assertNotNull(reloadedRefueling.getCreatedAt());
    }
}
