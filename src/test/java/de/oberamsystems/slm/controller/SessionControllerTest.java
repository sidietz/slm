package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SessionControllerTest {

    private SessionController controller;
    private SessionRepository sessionRepo;
    private CodingSessionRepository codingSessionRepo;
    private WorkingSessionRepository workingSessionRepo;
    private LearningSessionRepository learningSessionRepo;
    private GamingSessionRepository gamingSessionRepo;
    private ReadingSessionRepository readingSessionRepo;
    private MeditationSessionRepository meditationSessionRepo;
    private SportSessionRepository sportSessionRepo;
    private LearningItemRepository learningItemRepo;
    private GameRepository gameRepo;
    private DeviceRepository deviceRepo;
    private BookRepository bookRepo;
    private SportTypeRepository sportTypeRepo;
    private EmployerRepository employerRepo;

    @BeforeEach
    public void setup() throws Exception {
        controller = new SessionController();

        sessionRepo = mock(SessionRepository.class);
        codingSessionRepo = mock(CodingSessionRepository.class);
        workingSessionRepo = mock(WorkingSessionRepository.class);
        learningSessionRepo = mock(LearningSessionRepository.class);
        gamingSessionRepo = mock(GamingSessionRepository.class);
        readingSessionRepo = mock(ReadingSessionRepository.class);
        meditationSessionRepo = mock(MeditationSessionRepository.class);
        sportSessionRepo = mock(SportSessionRepository.class);
        learningItemRepo = mock(LearningItemRepository.class);
        gameRepo = mock(GameRepository.class);
        deviceRepo = mock(DeviceRepository.class);
        bookRepo = mock(BookRepository.class);
        sportTypeRepo = mock(SportTypeRepository.class);
        employerRepo = mock(EmployerRepository.class);

        setField("sessionRepo", sessionRepo);
        setField("codingSessionRepo", codingSessionRepo);
        setField("workingSessionRepo", workingSessionRepo);
        setField("learningSessionRepo", learningSessionRepo);
        setField("gamingSessionRepo", gamingSessionRepo);
        setField("readingSessionRepo", readingSessionRepo);
        setField("meditationSessionRepo", meditationSessionRepo);
        setField("sportSessionRepo", sportSessionRepo);
        setField("learningItemRepo", learningItemRepo);
        setField("gameRepo", gameRepo);
        setField("deviceRepo", deviceRepo);
        setField("bookRepo", bookRepo);
        setField("sportTypeRepo", sportTypeRepo);
        setField("employerRepo", employerRepo);
    }

    private void setField(String fieldName, Object val) throws Exception {
        java.lang.reflect.Field field = SessionController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, val);
    }

    @Test
    public void testViewSessionsAll() {
        List<Session> mockList = new ArrayList<>();
        CodingSession cs = new CodingSession();
        cs.setStart(LocalDateTime.now().minusHours(2));
        cs.setEnd(LocalDateTime.now());
        cs.setBreakDuration(Duration.ofMinutes(15));
        cs.setNetDuration(Duration.ofMinutes(105));
        cs.setFocusRating(5);
        mockList.add(cs);

        WorkingSession ws = new WorkingSession();
        ws.setStart(LocalDateTime.now().minusHours(4));
        ws.setEnd(LocalDateTime.now().minusHours(2));
        ws.setBreakDuration(Duration.ofMinutes(30));
        ws.setNetDuration(Duration.ofMinutes(90));
        ws.setFocusRating(4);
        mockList.add(ws);

        when(sessionRepo.findAll()).thenReturn(mockList);

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.viewSessions("ALL", model);

        assertEquals("sessions", view);
        assertEquals("ALL", model.getAttribute("activeType"));
        assertEquals(2, model.getAttribute("totalSessionsCount"));
        assertEquals("3h 15m", model.getAttribute("totalNetDuration"));
        assertEquals("45m", model.getAttribute("totalBreakDuration"));
        assertEquals("4.5", model.getAttribute("avgFocusRating"));
        assertNotNull(model.getAttribute("codingSession"));
        assertNotNull(model.getAttribute("workingSession"));
    }

    @Test
    public void testViewSessionsFilterType() {
        List<Session> mockList = new ArrayList<>();
        CodingSession cs = new CodingSession();
        cs.setStart(LocalDateTime.now().minusHours(1));
        cs.setEnd(LocalDateTime.now());
        mockList.add(cs);

        WorkingSession ws = new WorkingSession();
        ws.setStart(LocalDateTime.now().minusHours(3));
        ws.setEnd(LocalDateTime.now().minusHours(1));
        mockList.add(ws);

        when(sessionRepo.findAll()).thenReturn(mockList);

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.viewSessions("CODING", model);

        assertEquals("sessions", view);
        assertEquals("CODING", model.getAttribute("activeType"));
        assertEquals(1, model.getAttribute("totalSessionsCount"));
    }

    @Test
    public void testCodingSessionsViewAndSubmit() {
        when(codingSessionRepo.findAll()).thenReturn(new ArrayList<>());

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.viewCodingSessions(model);
        assertEquals("coding-sessions", view);
        assertNotNull(model.getAttribute("codingSession"));

        CodingSession cs = new CodingSession();
        cs.setProject("SLM");
        String redirect = controller.submitCodingSessionDirect(cs, 20L);
        assertEquals("redirect:/coding-sessions", redirect);
        assertEquals(Duration.ofMinutes(20), cs.getBreakDuration());
        verify(codingSessionRepo).save(cs);

        CodingSession cs2 = new CodingSession();
        String redirect2 = controller.submitCodingSession(cs2, 10L);
        assertEquals("redirect:/sessions?type=CODING", redirect2);
        assertEquals(Duration.ofMinutes(10), cs2.getBreakDuration());
        verify(codingSessionRepo).save(cs2);
    }

    @Test
    public void testWorkingSessionsViewAndSubmit() {
        when(workingSessionRepo.findAll()).thenReturn(new ArrayList<>());
        when(employerRepo.findAll()).thenReturn(new ArrayList<>());

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.viewWorkingSessions(model);
        assertEquals("working-sessions", view);
        assertNotNull(model.getAttribute("workingSession"));
        assertNotNull(model.getAttribute("employers"));

        WorkingSession ws = new WorkingSession();
        ws.setProject("Client Project");
        String redirect = controller.submitWorkingSessionDirect(ws, 30L);
        assertEquals("redirect:/working-sessions", redirect);
        assertEquals(Duration.ofMinutes(30), ws.getBreakDuration());
        verify(workingSessionRepo).save(ws);

        WorkingSession ws2 = new WorkingSession();
        String redirect2 = controller.submitWorkingSession(ws2, 15L);
        assertEquals("redirect:/sessions?type=WORKING", redirect2);
        assertEquals(Duration.ofMinutes(15), ws2.getBreakDuration());
        verify(workingSessionRepo).save(ws2);
    }

    @Test
    public void testSubmitOtherSessionTypes() {
        // Learning
        LearningSession ls = new LearningSession();
        LearningItem item = new LearningItem();
        item.setId(42L);
        ls.setLearningItem(item);
        when(learningItemRepo.findById(42L)).thenReturn(Optional.of(item));

        String redirectLearn = controller.submitLearningSession(ls, 10L);
        assertEquals("redirect:/sessions?type=LEARNING", redirectLearn);
        assertEquals(Duration.ofMinutes(10), ls.getBreakDuration());
        verify(learningItemRepo).save(item);
        verify(learningSessionRepo).save(ls);

        // Gaming
        GamingSession gs = new GamingSession();
        String redirectGame = controller.submitGamingSession(gs, 15L);
        assertEquals("redirect:/sessions?type=GAMING", redirectGame);
        assertEquals(Duration.ofMinutes(15), gs.getBreakDuration());
        verify(gamingSessionRepo).save(gs);

        // Reading
        ReadingSession rs = new ReadingSession();
        rs.setStart(LocalDateTime.now().minusHours(2));
        rs.setEnd(LocalDateTime.now());
        rs.setStartPageCount(10);
        rs.setEndPageCount(70);
        String redirectRead = controller.submitReadingSession(rs, 20L); // 120 - 20 = 100 min, 60 pages -> 36 p/h
        assertEquals("redirect:/sessions?type=READING", redirectRead);
        assertEquals(Duration.ofMinutes(20), rs.getBreakDuration());
        assertEquals(36.0f, rs.getReadingSpeed());
        verify(readingSessionRepo).save(rs);

        // Meditation
        MeditationSession ms = new MeditationSession();
        String redirectMed = controller.submitMeditationSession(ms, 0L);
        assertEquals("redirect:/sessions?type=MEDITATION", redirectMed);
        verify(meditationSessionRepo).save(ms);

        // Sport
        SportSession ss = new SportSession();
        String redirectSport = controller.submitSportSession(ss, 5L);
        assertEquals("redirect:/sessions?type=SPORT", redirectSport);
        assertEquals(Duration.ofMinutes(5), ss.getBreakDuration());
        verify(sportSessionRepo).save(ss);
    }

    @Test
    public void testDeleteSession() {
        String redirect = controller.deleteSession("0192e200-0000-7000-8000-000000000001", "CODING");
        assertEquals("redirect:/sessions?type=CODING", redirect);
        verify(sessionRepo).deleteById("0192e200-0000-7000-8000-000000000001");
    }
}
