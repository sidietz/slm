package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
@Table(name = "learning_session")
public class LearningSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name="learning_item_id", referencedColumnName = "id")
	private LearningItem learningItem;
	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable=false)
	private Duration duration;
	private String comment;

	public LearningSession() {
	}

	public LearningSession(Long id, LearningItem learningItem, LocalDateTime start, LocalDateTime end,
			Duration duration, String comment) {
		super();
		this.id = id;
		this.learningItem = learningItem;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.comment = comment;
	}

	public Long getId() {
		return id;
	}

	public LearningItem getLearningItem() {
		return learningItem;
	}

	public void setLearningItem(LearningItem learningItem) {
		this.learningItem = learningItem;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
