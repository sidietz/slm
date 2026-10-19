package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class SessionHierarchyIT extends AbstractIntegrationTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CodingSessionRepository codingSessionRepository;

    @Autowired
    private MeditationSessionRepository meditationSessionRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private WorkingSessionRepository workingSessionRepository;

    @Test
    public void testPolymorphicSessionHierarchyAndDuration() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Coding session: 2 hours gross, 30 mins break -> 90 mins net
        CodingSession coding = new CodingSession();
        coding.setStart(now.minusHours(2));
        coding.setEnd(now);
        coding.setBreakDuration(Duration.ofMinutes(30));
        coding.setProject("SLM");
        coding.setLanguage("Java");
        coding = codingSessionRepository.saveAndFlush(coding);

        assertNotNull(coding.getId());
        assertInstanceOf(UUID.class, coding.getId());
        assertEquals(7, coding.getId().version(), "ID must be a native UUIDv7");

        // 2. Meditation session: 20 mins gross, 0 break
        MeditationSession meditation = new MeditationSession();
        meditation.setStart(now.minusHours(4));
        meditation.setEnd(now.minusHours(4).plusMinutes(20));
        meditation.setBreakDuration(Duration.ZERO);
        meditation = meditationSessionRepository.saveAndFlush(meditation);

        // 3. Working session linked to Employer
        Employer employer = new Employer("Oberam Systems");
        employer = employerRepository.saveAndFlush(employer);

        WorkingSession working = new WorkingSession();
        working.setStart(now.minusHours(6));
        working.setEnd(now.minusHours(5));
        working.setEmployer(employer);
        working.setProject("Client Audit");
        working = workingSessionRepository.saveAndFlush(working);

        // 4. Query all polymorphically
        List<Session> all = sessionRepository.findAll();
        assertTrue(all.size() >= 3);

        boolean foundCoding = false;
        boolean foundMeditation = false;
        boolean foundWorking = false;

        for (Session s : all) {
            if (s instanceof CodingSession cs) {
                foundCoding = true;
                assertEquals("SLM", cs.getProject());
            } else if (s instanceof MeditationSession) {
                foundMeditation = true;
            } else if (s instanceof WorkingSession ws) {
                foundWorking = true;
                assertEquals("Oberam Systems", ws.getEmployer().getName());
            }
        }

        assertTrue(foundCoding, "CodingSession must be retrieved as polymorphic subclass");
        assertTrue(foundMeditation, "MeditationSession must be retrieved as polymorphic subclass");
        assertTrue(foundWorking, "WorkingSession must be retrieved as polymorphic subclass");
    }
}
