package de.oberamsystems.slm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
@Table(name = "lecture_note")
public class LectureNote {

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@OneToOne(optional = true)
	@JoinColumn(name = "lecture_session_id")
	private LectureSession lectureSession;

	@Column(name = "title", nullable = false, columnDefinition = "TEXT")
	private String title;

	@Column(name = "lecture_number")
	private Integer lectureNumber;

	@Column(name = "lecture_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate lectureDate;

	@Column(name = "content_markdown", columnDefinition = "TEXT")
	private String contentMarkdown;

	@Column(name = "key_concepts", columnDefinition = "TEXT")
	private String keyConcepts;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "has_homework")
	private Boolean hasHomework = false;

	@Column(name = "homework_todo", columnDefinition = "TEXT")
	private String homeworkTodo;

	@Column(name = "exam_relevance_rating")
	private Integer examRelevanceRating;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "last_updated")
	private LocalDateTime lastUpdated;

	public LectureNote() {
	}

	public LectureNote(Course course, String title, Integer lectureNumber, LocalDate lectureDate, String contentMarkdown) {
		this.course = course;
		this.title = title;
		this.lectureNumber = lectureNumber;
		this.lectureDate = lectureDate;
		this.contentMarkdown = contentMarkdown;
	}

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.lastUpdated = LocalDateTime.now();
		if (this.hasHomework == null) {
			this.hasHomework = false;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.lastUpdated = LocalDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public LectureSession getLectureSession() {
		return lectureSession;
	}

	public void setLectureSession(LectureSession lectureSession) {
		this.lectureSession = lectureSession;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getLectureNumber() {
		return lectureNumber;
	}

	public void setLectureNumber(Integer lectureNumber) {
		this.lectureNumber = lectureNumber;
	}

	public LocalDate getLectureDate() {
		return lectureDate;
	}

	public void setLectureDate(LocalDate lectureDate) {
		this.lectureDate = lectureDate;
	}

	public String getContentMarkdown() {
		return contentMarkdown;
	}

	public void setContentMarkdown(String contentMarkdown) {
		this.contentMarkdown = contentMarkdown;
	}

	public String getKeyConcepts() {
		return keyConcepts;
	}

	public void setKeyConcepts(String keyConcepts) {
		this.keyConcepts = keyConcepts;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Boolean getHasHomework() {
		return hasHomework;
	}

	public void setHasHomework(Boolean hasHomework) {
		this.hasHomework = hasHomework;
	}

	public String getHomeworkTodo() {
		return homeworkTodo;
	}

	public void setHomeworkTodo(String homeworkTodo) {
		this.homeworkTodo = homeworkTodo;
	}

	public Integer getExamRelevanceRating() {
		return examRelevanceRating;
	}

	public void setExamRelevanceRating(Integer examRelevanceRating) {
		this.examRelevanceRating = examRelevanceRating;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		return String.format("LectureNote [id=%s, course=%s, lectureNumber=%s, title=%s]",
				id, course, lectureNumber, title);
	}
}
