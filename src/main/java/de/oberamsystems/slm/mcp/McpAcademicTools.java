package de.oberamsystems.slm.mcp;

import de.oberamsystems.slm.model.*;
import de.oberamsystems.slm.util.CourseComparator;
import de.oberamsystems.slm.util.DurationFormatter;
import de.oberamsystems.slm.util.SemesterComparator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class McpAcademicTools {

	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private LectureSessionRepository lectureSessionRepo;

	@Autowired
	private LectureNoteRepository lectureNoteRepo;

	@Autowired
	private HumanRepository humanRepo;

	@Tool(description = "Retrieve all distinct academic semesters, sorted chronologically descending (most recent semester first)")
	public List<String> getSemesters() {
		List<String> semesters = new ArrayList<>(courseRepo.findDistinctSemesters());
		semesters.sort(SemesterComparator.DESCENDING);
		return semesters;
	}

	@Tool(description = "Retrieve list of university courses, optionally filtered by semester (e.g. 'WS 2026/27')")
	public List<Map<String, Object>> getCourses(
			@ToolParam(description = "Semester filter (e.g. 'WS 2026/27', 'SS 2027', or null for all)", required = false) String semester) {

		List<Course> list = (semester != null && !semester.isBlank() && !"ALL".equalsIgnoreCase(semester))
				? new ArrayList<>(courseRepo.findAllBySemesterOrderByTitleAsc(semester))
				: new ArrayList<>(courseRepo.findAll());
		list.sort(CourseComparator.INSTANCE);

		return list.stream().map(c -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", c.getId());
			m.put("title", c.getTitle());
			m.put("code", c.getCode());
			m.put("semester", c.getSemester());
			m.put("ectsCredits", c.getEctsCredits());
			m.put("lecturer", c.getLecturer() != null ? c.getLecturer().getName() : null);
			m.put("defaultRoom", c.getDefaultRoom());
			m.put("examDate", c.getExamDate() != null ? c.getExamDate().toString() : null);
			m.put("examLocation", c.getExamLocation());
			m.put("grade", c.getGrade());
			m.put("status", c.getStatus() != null ? c.getStatus().name() : null);
			return m;
		}).collect(Collectors.toList());
	}

	@Tool(description = "Get detailed information for a university course including syllabus, notes, and attended lectures")
	public Map<String, Object> getCourseDetails(
			@ToolParam(description = "Course ID", required = true) Long courseId) {

		Course course = courseRepo.findById(courseId).orElse(null);
		if (course == null) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Course not found for ID: " + courseId);
			return err;
		}

		List<LectureNote> notes = lectureNoteRepo.findByCourseIdOrderByLectureNumberAsc(courseId);
		List<LectureSession> sessions = lectureSessionRepo.findByCourseIdOrderByLectureNumberAsc(courseId);

		Map<String, Object> res = new HashMap<>();
		res.put("id", course.getId());
		res.put("title", course.getTitle());
		res.put("code", course.getCode());
		res.put("semester", course.getSemester());
		res.put("ectsCredits", course.getEctsCredits());
		res.put("lecturer", course.getLecturer() != null ? course.getLecturer().getName() : null);
		res.put("syllabus", course.getSyllabus());
		res.put("defaultRoom", course.getDefaultRoom());
		res.put("examDate", course.getExamDate() != null ? course.getExamDate().toString() : null);
		res.put("examLocation", course.getExamLocation());
		res.put("notesCount", notes.size());
		res.put("sessionsCount", sessions.size());

		List<Map<String, Object>> notesSummary = notes.stream().map(n -> {
			Map<String, Object> nm = new HashMap<>();
			nm.put("id", n.getId().toString());
			nm.put("lectureNumber", n.getLectureNumber());
			nm.put("title", n.getTitle());
			nm.put("lectureDate", n.getLectureDate() != null ? n.getLectureDate().toString() : null);
			nm.put("keyConcepts", n.getKeyConcepts());
			nm.put("examRelevance", n.getExamRelevanceRating());
			nm.put("hasHomework", n.getHasHomework());
			return nm;
		}).collect(Collectors.toList());
		res.put("notes", notesSummary);

		return res;
	}

	@Tool(description = "Log attendance at a university lecture session contributing to Net Focused Time")
	public Map<String, Object> logLectureSession(
			@ToolParam(description = "Course ID", required = true) Long courseId,
			@ToolParam(description = "Lecture sequential number (e.g. 1, 2, 3)", required = false) Integer lectureNumber,
			@ToolParam(description = "Lecture topic title (e.g. 'Indexing and B-Trees')", required = true) String topic,
			@ToolParam(description = "Room or online meeting URL", required = false) String room,
			@ToolParam(description = "Start timestamp in ISO-8601 (e.g. '2026-10-19T10:15:00')", required = true) String startTime,
			@ToolParam(description = "End timestamp in ISO-8601 (e.g. '2026-10-19T11:45:00')", required = true) String endTime,
			@ToolParam(description = "Break or pause duration in minutes (default 0)", required = false) Integer breakMinutes,
			@ToolParam(description = "Attention / focus rating from 1 to 5 (default 4)", required = false) Integer focusRating,
			@ToolParam(description = "Comment or summary notes taken during the lecture", required = false) String comment) {

		Course course = courseRepo.findById(courseId).orElse(null);
		if (course == null) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Course not found for ID: " + courseId);
			return err;
		}

		LectureSession ls = new LectureSession();
		ls.setCourse(course);
		ls.setLectureNumber(lectureNumber != null ? lectureNumber : 1);
		ls.setTopic(topic);
		ls.setRoom(room != null && !room.isBlank() ? room : course.getDefaultRoom());
		ls.setStart(LocalDateTime.parse(startTime));
		ls.setEnd(LocalDateTime.parse(endTime));
		ls.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes != null ? breakMinutes.longValue() : 0L));
		ls.setFocusRating((focusRating != null && focusRating >= 1 && focusRating <= 5) ? focusRating : 4);
		ls.setComment(comment);

		lectureSessionRepo.save(ls);

		Map<String, Object> res = new HashMap<>();
		res.put("id", ls.getId());
		res.put("course", course.getTitle());
		res.put("topic", ls.getTopic());
		res.put("start", ls.getStart().toString());
		res.put("end", ls.getEnd().toString());
		res.put("status", "SUCCESS");
		return res;
	}

	@Tool(description = "Save or update lecture notes in rich Markdown format with key concepts summary and exam relevance rating")
	public Map<String, Object> saveLectureNote(
			@ToolParam(description = "Course ID", required = true) Long courseId,
			@ToolParam(description = "Note title / lecture topic", required = true) String title,
			@ToolParam(description = "Full lecture notes in Markdown format", required = true) String markdownContent,
			@ToolParam(description = "Lecture sequential number (e.g. 1, 2, 3)", required = false) Integer lectureNumber,
			@ToolParam(description = "Date of the lecture (ISO date YYYY-MM-DD, defaults to today)", required = false) String lectureDate,
			@ToolParam(description = "Key concepts or keywords (comma-separated tags)", required = false) String keyConcepts,
			@ToolParam(description = "2-3 sentence executive takeaway summary", required = false) String summary,
			@ToolParam(description = "Whether homework was assigned (default false)", required = false) Boolean hasHomework,
			@ToolParam(description = "Action items or homework exercises to complete", required = false) String homeworkTodo,
			@ToolParam(description = "Exam relevance rating from 1 (minor) to 5 (critical)", required = false) Integer examRating) {

		Course course = courseRepo.findById(courseId).orElse(null);
		if (course == null) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Course not found for ID: " + courseId);
			return err;
		}

		LectureNote note = new LectureNote();
		note.setCourse(course);
		note.setTitle(title);
		note.setContentMarkdown(markdownContent);
		note.setLectureNumber(lectureNumber != null ? lectureNumber : 1);
		note.setLectureDate(lectureDate != null ? LocalDate.parse(lectureDate) : LocalDate.now());
		note.setKeyConcepts(keyConcepts);
		note.setSummary(summary);
		note.setHasHomework(hasHomework != null ? hasHomework : false);
		note.setHomeworkTodo(homeworkTodo);
		note.setExamRelevanceRating(examRating != null ? examRating : 3);

		LectureNote saved = lectureNoteRepo.save(note);

		Map<String, Object> res = new HashMap<>();
		res.put("id", saved.getId().toString());
		res.put("course", course.getTitle());
		res.put("title", saved.getTitle());
		res.put("lectureNumber", saved.getLectureNumber());
		res.put("status", "SUCCESS");
		return res;
	}

	@Tool(description = "Search across all lecture notes by keyword in title, markdown content, or key concepts tags")
	public List<Map<String, Object>> searchLectureNotes(
			@ToolParam(description = "Search keyword or topic query", required = true) String query) {

		List<LectureNote> notes = lectureNoteRepo.searchNotes(query);
		return notes.stream().map(n -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", n.getId().toString());
			m.put("course", n.getCourse() != null ? n.getCourse().getTitle() : null);
			m.put("title", n.getTitle());
			m.put("lectureNumber", n.getLectureNumber());
			m.put("lectureDate", n.getLectureDate() != null ? n.getLectureDate().toString() : null);
			m.put("summary", n.getSummary());
			m.put("keyConcepts", n.getKeyConcepts());
			m.put("examRelevance", n.getExamRelevanceRating());
			return m;
		}).collect(Collectors.toList());
	}
}
