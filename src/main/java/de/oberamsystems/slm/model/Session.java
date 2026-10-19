package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;
import org.hibernate.generator.EventType;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
@Table(name = "session")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "session_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Session {

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@Column(name = "status", columnDefinition = "TEXT")
	private String status = "COMPLETED";

	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;

	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;

	@Type(PostgreSQLIntervalType.class)
	@Column(name = "break_duration", columnDefinition = "interval")
	private Duration breakDuration = Duration.ZERO;

	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	private Duration duration;

	@Type(PostgreSQLIntervalType.class)
	@Column(name = "net_duration", columnDefinition = "interval", insertable = false, updatable = false)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	private Duration netDuration;

	@Column(name = "focus_rating")
	private Integer focusRating;

	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;

	@Column(name = "outcome_summary", columnDefinition = "TEXT")
	private String outcomeSummary;

	public Session() {
	}

	public Session(UUID id, String status, LocalDateTime start, LocalDateTime end, Duration breakDuration,
			Duration duration, Duration netDuration, Integer focusRating, String comment, String outcomeSummary) {
		this.id = id;
		this.status = status;
		this.start = start;
		this.end = end;
		this.breakDuration = breakDuration;
		this.duration = duration;
		this.netDuration = netDuration;
		this.focusRating = focusRating;
		this.comment = comment;
		this.outcomeSummary = outcomeSummary;
	}

	@PrePersist
	public void prePersist() {
		if (this.id == null) {
			this.id = UUID.ofEpochMillis(System.currentTimeMillis());
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Duration getBreakDuration() {
		return breakDuration;
	}

	public void setBreakDuration(Duration breakDuration) {
		this.breakDuration = breakDuration;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public Duration getNetDuration() {
		return netDuration;
	}

	public void setNetDuration(Duration netDuration) {
		this.netDuration = netDuration;
	}

	public Integer getFocusRating() {
		return focusRating;
	}

	public void setFocusRating(Integer focusRating) {
		this.focusRating = focusRating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getOutcomeSummary() {
		return outcomeSummary;
	}

	public void setOutcomeSummary(String outcomeSummary) {
		this.outcomeSummary = outcomeSummary;
	}

	@Override
	public String toString() {
		return String.format(
				"Session [id=%s, status=%s, start=%s, end=%s, breakDuration=%s, duration=%s, netDuration=%s, focusRating=%s, comment=%s, outcomeSummary=%s]",
				id, status, start, end, breakDuration, duration, netDuration, focusRating, comment, outcomeSummary);
	}
}
