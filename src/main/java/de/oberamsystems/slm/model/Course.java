package de.oberamsystems.slm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
@Table(name = "course")
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false, columnDefinition = "TEXT")
	private String title;

	@Column(name = "code", columnDefinition = "TEXT")
	private String code;

	@Column(name = "semester", columnDefinition = "TEXT")
	private String semester;

	@Column(name = "ects_credits", precision = 3, scale = 1)
	private BigDecimal ectsCredits;

	@ManyToOne
	@JoinColumn(name = "lecturer_id")
	private Human lecturer;

	@Column(name = "default_room", columnDefinition = "TEXT")
	private String defaultRoom;

	@Column(name = "meeting_link", columnDefinition = "TEXT")
	private String meetingLink;

	@Column(name = "syllabus", columnDefinition = "TEXT")
	private String syllabus;

	@Column(name = "exam_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime examDate;

	@Column(name = "exam_location", columnDefinition = "TEXT")
	private String examLocation;

	@Column(name = "grade", precision = 3, scale = 2)
	private BigDecimal grade;

	@Column(name = "exam_passed")
	private Boolean examPassed;

	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	@Column(name = "status")
	private StatusEnum status = StatusEnum.IN_PROGRESS;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "last_updated")
	private LocalDateTime lastUpdated;

	public Course() {
	}

	public Course(String title, String code, String semester, BigDecimal ectsCredits, Human lecturer) {
		this.title = title;
		this.code = code;
		this.semester = semester;
		this.ectsCredits = ectsCredits;
		this.lecturer = lecturer;
	}

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.lastUpdated = LocalDateTime.now();
		if (this.status == null) {
			this.status = StatusEnum.IN_PROGRESS;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.lastUpdated = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}

	public BigDecimal getEctsCredits() {
		return ectsCredits;
	}

	public void setEctsCredits(BigDecimal ectsCredits) {
		this.ectsCredits = ectsCredits;
	}

	public Human getLecturer() {
		return lecturer;
	}

	public void setLecturer(Human lecturer) {
		this.lecturer = lecturer;
	}

	public String getDefaultRoom() {
		return defaultRoom;
	}

	public void setDefaultRoom(String defaultRoom) {
		this.defaultRoom = defaultRoom;
	}

	public String getMeetingLink() {
		return meetingLink;
	}

	public void setMeetingLink(String meetingLink) {
		this.meetingLink = meetingLink;
	}

	public String getSyllabus() {
		return syllabus;
	}

	public void setSyllabus(String syllabus) {
		this.syllabus = syllabus;
	}

	public LocalDateTime getExamDate() {
		return examDate;
	}

	public void setExamDate(LocalDateTime examDate) {
		this.examDate = examDate;
	}

	public String getExamLocation() {
		return examLocation;
	}

	public void setExamLocation(String examLocation) {
		this.examLocation = examLocation;
	}

	public BigDecimal getGrade() {
		return grade;
	}

	public void setGrade(BigDecimal grade) {
		this.grade = grade;
	}

	public Boolean getExamPassed() {
		return examPassed;
	}

	public void setExamPassed(Boolean examPassed) {
		this.examPassed = examPassed;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
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

	public BigDecimal getTargetWorkloadHours() {
		return ectsCredits != null ? ectsCredits.multiply(BigDecimal.valueOf(30)) : null;
	}

	public boolean isNonAcademicCourse() {
		if (semester == null) return false;
		String upper = semester.trim().toUpperCase();
		return upper.equals("OTHER") || upper.equals("MOOC") || upper.equals("VHS") || upper.startsWith("COURSERA") || upper.startsWith("EDX");
	}

	@Override
	public String toString() {
		return String.format("Course [id=%s, title=%s, code=%s, semester=%s, ectsCredits=%s]",
				id, title, code, semester, ectsCredits);
	}
}
