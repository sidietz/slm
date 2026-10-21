package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class DoctorControllerIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SpecialityRepository specialityRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testSpecialityManagementFlow() throws Exception {
        // 1. GET /specialities
        mockMvc.perform(get("/specialities"))
                .andExpect(status().isOk())
                .andExpect(view().name("specialities"))
                .andExpect(model().attributeExists("speciality", "specialities"));

        // 2. POST /specialities
        mockMvc.perform(post("/specialities")
                        .with(csrf())
                        .param("title", "Cardiology"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/specialities"));
        assertEquals(1, specialityRepository.count());

        // 3. Alternative view /speciality and /speciality.html
        mockMvc.perform(get("/speciality"))
                .andExpect(status().isOk())
                .andExpect(view().name("speciality"))
                .andExpect(model().attributeExists("specialities"))
                .andExpect(content().string(containsString("Cardiology")));

        mockMvc.perform(get("/speciality.html"))
                .andExpect(status().isOk())
                .andExpect(view().name("speciality"));
    }

    @Test
    void testDoctorAndPostGISDistanceCalculationFlow() throws Exception {
        // 1. Prepare Base Home Point (Munich) and Doctor Location Point (Starnberg)
        Point homePoint = new Point(null, "Home Munich", "Primary Residence", 48.137154, 11.576124, true);
        homePoint = pointRepository.saveAndFlush(homePoint);

        Point practicePoint = new Point(null, "Starnberg Clinic", "Medical Practice", 47.998472, 11.342168, false);
        practicePoint = pointRepository.saveAndFlush(practicePoint);

        Speciality speciality = new Speciality(null, "Neurology");
        speciality = specialityRepository.saveAndFlush(speciality);

        // 2. GET /doctors
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors"))
                .andExpect(model().attributeExists("specialities", "doctors", "points", "doctor"));

        // 3. POST /doctors with PostGIS geodesic distance calculation
        mockMvc.perform(post("/doctors")
                        .with(csrf())
                        .param("firstname", "Alexander")
                        .param("lastname", "Fleming")
                        .param("speciality", speciality.getId().toString())
                        .param("point", practicePoint.getId().toString())
                        .param("basePoint", homePoint.getId().toString())
                        .param("city", "Starnberg")
                        .param("plz", "82319"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));

        assertEquals(1, doctorRepository.count());
        Doctor savedDoctor = doctorRepository.findAll().getFirst();
        assertEquals("Fleming", savedDoctor.getLastname());
        assertNotNull(savedDoctor.getAirDistance());

        // 4. GET /doctor and /doctor.html
        mockMvc.perform(get("/doctor"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor"))
                .andExpect(model().attributeExists("doctors"))
                .andExpect(content().string(containsString("Fleming")));

        mockMvc.perform(get("/doctor.html"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor"));
    }

    @Test
    void testAppointmentManagementAndDateFilterFlow() throws Exception {
        Speciality spec = new Speciality(null, "Orthopedics");
        spec = specialityRepository.saveAndFlush(spec);

        Doctor doctor = new Doctor();
        doctor.setFirstname("Gregory");
        doctor.setLastname("House");
        doctor.setSpeciality(spec);
        doctor = doctorRepository.saveAndFlush(doctor);

        // 1. GET /appointments
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointments"))
                .andExpect(model().attributeExists("appointment", "doctors", "appointments"));

        // 2. POST /appointments
        mockMvc.perform(post("/appointments")
                        .with(csrf())
                        .param("doctor", doctor.getId().toString())
                        .param("title", "Annual Health Checkup")
                        .param("departure", "2026-10-19T08:30")
                        .param("arrival", "2026-10-19T09:00")
                        .param("start", "2026-10-19T09:15")
                        .param("end", "2026-10-19T10:00")
                        .param("arrivalHome", "2026-10-19T10:30")
                        .param("comment", "Routine blood work and checkup"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"));
        assertEquals(1, appointmentRepository.count());

        // 3. GET /appointment with date range filter
        mockMvc.perform(get("/appointment")
                        .param("fromDate", "2026-10-01T00:00:00")
                        .param("toDate", "2026-10-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointment"))
                .andExpect(model().attributeExists("appointments", "fromDate", "toDate", "minDate", "maxDate"))
                .andExpect(content().string(containsString("House")))
                .andExpect(content().string(containsString("Routine blood work and checkup")));
    }

    @Test
    void testUpdateSpecialityFlow() throws Exception {
        Speciality spec = new Speciality(null, "Pediatrics");
        spec = specialityRepository.saveAndFlush(spec);
        Long specId = spec.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/specialities")
                        .with(csrf())
                        .param("id", specId.toString())
                        .param("title", "Pediatric Medicine"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/specialities"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, specialityRepository.count(), "Editing speciality must not duplicate row");
        Speciality updated = specialityRepository.findById(specId).orElseThrow();
        assertEquals("Pediatric Medicine", updated.getTitle());
    }

    @Test
    void testUpdateDoctorFlow() throws Exception {
        Speciality spec = new Speciality(null, "Dermatology");
        spec = specialityRepository.saveAndFlush(spec);

        Doctor doc = new Doctor();
        doc.setFirstname("Paul");
        doc.setLastname("Ehrlich");
        doc.setSpeciality(spec);
        doc.setCity("Frankfurt");
        doc.setPlz("60323");
        doc = doctorRepository.saveAndFlush(doc);
        Long docId = doc.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/doctors")
                        .with(csrf())
                        .param("id", docId.toString())
                        .param("firstname", "Paul")
                        .param("lastname", "Ehrlich")
                        .param("speciality", spec.getId().toString())
                        .param("city", "Frankfurt am Main")
                        .param("plz", "60596")
                        .param("place", "Theodor-Stern-Kai 7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, doctorRepository.count(), "Editing doctor must not duplicate row");
        Doctor updated = doctorRepository.findById(docId).orElseThrow();
        assertEquals("Frankfurt am Main", updated.getCity());
        assertEquals("60596", updated.getPlz());
        assertEquals("Theodor-Stern-Kai 7", updated.getPlace());
    }

    @Test
    void testUpdateAppointmentFlow() throws Exception {
        Speciality spec = new Speciality(null, "Orthopedics");
        spec = specialityRepository.saveAndFlush(spec);

        Doctor doc = new Doctor();
        doc.setFirstname("Max");
        doc.setLastname("Planck");
        doc.setSpeciality(spec);
        doc = doctorRepository.saveAndFlush(doc);

        Appointment appt = new Appointment();
        appt.setDoctor(doc);
        appt.setStart(LocalDateTime.of(2026, 11, 5, 10, 0));
        appt.setEnd(LocalDateTime.of(2026, 11, 5, 10, 30));
        appt.setComment("Initial consultation");
        appt = appointmentRepository.saveAndFlush(appt);
        Long apptId = appt.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/appointments")
                        .with(csrf())
                        .param("id", apptId.toString())
                        .param("doctor", doc.getId().toString())
                        .param("start", "2026-11-05T10:00")
                        .param("end", "2026-11-05T10:45")
                        .param("comment", "Comprehensive knee MRI follow-up"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, appointmentRepository.count(), "Editing appointment must not duplicate row");
        Appointment updated = appointmentRepository.findById(apptId).orElseThrow();
        assertEquals("Comprehensive knee MRI follow-up", updated.getComment());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 45), updated.getEnd());
    }

    @Test
    void testModalCreateDoctorAndAppointmentWithEmptyIdParam() throws Exception {
        // 1. Speciality via modal with empty id
        mockMvc.perform(post("/specialities")
                        .with(csrf())
                        .param("id", "")
                        .param("title", "Neurology"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/specialities"));
        assertEquals(1, specialityRepository.count());
        Speciality spec = specialityRepository.findAll().getFirst();

        // 2. Doctor via modal with empty id and explicit speciality
        mockMvc.perform(post("/doctors")
                        .with(csrf())
                        .param("id", "")
                        .param("firstname", "Sigmund")
                        .param("lastname", "Freud")
                        .param("speciality", spec.getId().toString())
                        .param("city", "Vienna"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));
        assertEquals(1, doctorRepository.count());

        // 3. Doctor via modal with empty id and omitted speciality (tests fallback)
        mockMvc.perform(post("/doctors")
                        .with(csrf())
                        .param("id", "")
                        .param("firstname", "Carl")
                        .param("lastname", "Jung")
                        .param("city", "Zurich"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));
        assertEquals(2, doctorRepository.count());
        Doctor doc = doctorRepository.findAll().getFirst();

        // 4. Appointment via modal with empty id and explicit doctor
        mockMvc.perform(post("/appointments")
                        .with(csrf())
                        .param("id", "")
                        .param("doctor", doc.getId().toString())
                        .param("start", "2026-11-10T14:00")
                        .param("end", "2026-11-10T15:00")
                        .param("comment", "Checkup"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"));
        assertEquals(1, appointmentRepository.count());

        // 5. Appointment via modal with empty id and omitted doctor (tests fallback)
        mockMvc.perform(post("/appointments")
                        .with(csrf())
                        .param("id", "")
                        .param("start", "2026-11-12T09:00")
                        .param("end", "2026-11-12T09:30")
                        .param("comment", "Routine"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"));
        assertEquals(2, appointmentRepository.count());
    }
}
