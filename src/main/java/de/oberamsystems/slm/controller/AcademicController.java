package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import de.oberamsystems.slm.util.CourseComparator;
import de.oberamsystems.slm.util.SemesterComparator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AcademicController {

	private static final Logger log = LoggerFactory.getLogger(AcademicController.class);

	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private LectureSessionRepository lectureSessionRepo;

	@Autowired
	private LectureNoteRepository lectureNoteRepo;

	@Autowired
	private HumanRepository humanRepo;

	@GetMapping("/courses")
	public String listCourses(@RequestParam(required = false) String semester, Model model) {
		List<String> semesters = new ArrayList<>(courseRepo.findDistinctSemesters());
		semesters.sort(SemesterComparator.DESCENDING);

		List<Course> courses;
		if (semester != null && !semester.isBlank() && !"ALL".equalsIgnoreCase(semester)) {
			courses = new ArrayList<>(courseRepo.findAllBySemesterOrderByTitleAsc(semester));
		} else {
			courses = new ArrayList<>(courseRepo.findAll());
		}
		courses.sort(CourseComparator.INSTANCE);

		BigDecimal totalEcts = courses.stream()
				.map(Course::getEctsCredits)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		LocalDateTime now = LocalDateTime.now();
		List<Course> upcomingExams = courseRepo.findByExamDateBetweenOrderByExamDateAsc(now, now.plusDays(90));

		model.addAttribute("courses", courses);
		model.addAttribute("semesters", semesters);
		model.addAttribute("selectedSemester", semester != null ? semester : "ALL");
		model.addAttribute("totalEcts", totalEcts);
		model.addAttribute("upcomingExams", upcomingExams);
		model.addAttribute("newCourse", new Course());
		model.addAttribute("lecturers", humanRepo.findByCategory(HumanCategory.LECTURER));

		return "courses";
	}

	@GetMapping("/courses/{id}")
	public String viewCourse(@PathVariable Long id, Model model) {
		Course course = courseRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
		List<LectureSession> sessions = lectureSessionRepo.findByCourseIdOrderByStartDesc(id);
		List<LectureNote> notes = lectureNoteRepo.findByCourseIdOrderByLectureNumberAsc(id);

		model.addAttribute("course", course);
		model.addAttribute("sessions", sessions);
		model.addAttribute("notes", notes);
		model.addAttribute("lecturers", humanRepo.findByCategory(HumanCategory.LECTURER));
		model.addAttribute("newSession", new LectureSession(course, sessions.size() + 1, "", course.getDefaultRoom()));

		return "course-detail";
	}

	@PostMapping("/courses")
	public String saveCourse(@ModelAttribute Course course, @RequestParam(required = false) Long lecturerId) {
		Long lid = (lecturerId != null && lecturerId > 0) ? lecturerId :
				(course.getLecturer() != null && course.getLecturer().getId() > 0 ? course.getLecturer().getId() : null);
		if (lid != null) {
			humanRepo.findById(lid).ifPresent(course::setLecturer);
		} else {
			course.setLecturer(null);
		}
		if (course.getId() != null && (course.getId() <= 0 || !courseRepo.existsById(course.getId()))) {
			course.setId(null);
		}
		courseRepo.save(course);
		return "redirect:/courses";
	}

	@PostMapping("/courses/{id}/edit")
	public String updateCourse(@PathVariable Long id, @ModelAttribute Course course, @RequestParam(required = false) Long lecturerId) {
		course.setId(id);
		Long lid = (lecturerId != null && lecturerId > 0) ? lecturerId :
				(course.getLecturer() != null && course.getLecturer().getId() > 0 ? course.getLecturer().getId() : null);
		if (lid != null) {
			humanRepo.findById(lid).ifPresent(course::setLecturer);
		} else {
			course.setLecturer(null);
		}
		courseRepo.save(course);
		return "redirect:/courses/" + id;
	}

	@PostMapping("/courses/{id}/delete")
	public String deleteCourse(@PathVariable Long id) {
		courseRepo.deleteById(id);
		return "redirect:/courses";
	}

	@GetMapping("/courses/{courseId}/notes/new")
	public String newNoteForCourse(@PathVariable Long courseId,
			@RequestParam(required = false) UUID sessionId,
			Model model) {
		Course course = courseRepo.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
		LectureNote note = new LectureNote();
		note.setCourse(course);
		note.setLectureDate(LocalDate.now());

		if (sessionId != null) {
			lectureSessionRepo.findById(sessionId).ifPresent(s -> {
				note.setLectureSession(s);
				note.setLectureNumber(s.getLectureNumber());
				if (s.getTopic() != null) {
					note.setTitle(s.getTopic());
				}
			});
		}

		if (note.getLectureNumber() == null) {
			List<LectureNote> existingNotes = lectureNoteRepo.findByCourseIdOrderByLectureNumberAsc(courseId);
			note.setLectureNumber(existingNotes.size() + 1);
		}

		model.addAttribute("note", note);
		model.addAttribute("course", course);
		model.addAttribute("sessions", lectureSessionRepo.findByCourseIdOrderByLectureNumberAsc(courseId));
		return "lecture-note-edit";
	}

	@GetMapping("/notes/{id}")
	public String viewNote(@PathVariable UUID id, Model model) {
		LectureNote note = lectureNoteRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
		model.addAttribute("note", note);
		return "lecture-note-view";
	}

	@GetMapping("/notes/{id}/edit")
	public String editNote(@PathVariable UUID id, Model model) {
		LectureNote note = lectureNoteRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
		model.addAttribute("note", note);
		model.addAttribute("course", note.getCourse());
		model.addAttribute("sessions", lectureSessionRepo.findByCourseIdOrderByLectureNumberAsc(note.getCourse().getId()));
		return "lecture-note-edit";
	}

	@PostMapping("/notes")
	public String saveNote(@ModelAttribute LectureNote note, @RequestParam(required = false) UUID sessionId) {
		if (note.getCourse() != null && note.getCourse().getId() != null) {
			courseRepo.findById(note.getCourse().getId()).ifPresent(note::setCourse);
		}
		UUID sid = (sessionId != null) ? sessionId :
				(note.getLectureSession() != null && note.getLectureSession().getId() != null
						? note.getLectureSession().getId() : null);
		if (sid != null) {
			lectureSessionRepo.findById(sid).ifPresent(note::setLectureSession);
		} else {
			note.setLectureSession(null);
		}
		LectureNote saved = lectureNoteRepo.save(note);
		return "redirect:/notes/" + saved.getId();
	}

	@PostMapping("/notes/{id}/delete")
	public String deleteNote(@PathVariable UUID id) {
		LectureNote note = lectureNoteRepo.findById(id).orElse(null);
		Long courseId = (note != null && note.getCourse() != null) ? note.getCourse().getId() : null;
		lectureNoteRepo.deleteById(id);
		if (courseId != null) {
			return "redirect:/courses/" + courseId;
		}
		return "redirect:/courses";
	}
}
