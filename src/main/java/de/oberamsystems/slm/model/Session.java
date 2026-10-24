package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import de.oberamsystems.slm.config.TimeZoneConfig;
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

	@Column(name = "starttime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime start = OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES);

	@Column(name = "endtime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime end;

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

	public Session(UUID id, String status, OffsetDateTime start, OffsetDateTime end, Duration breakDuration,
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

	public OffsetDateTime getStart() {
		return start;
	}

	public void setStart(OffsetDateTime start) {
		this.start = start;
	}

	public OffsetDateTime getEnd() {
		return end;
	}

	public void setEnd(OffsetDateTime end) {
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

	public boolean isInProgress() {
		return "IN_PROGRESS".equalsIgnoreCase(this.status) || this.end == null;
	}

	public void stop(OffsetDateTime stopTime, Duration breakDuration, Integer focusRating, String outcomeSummary, String comment) {
		this.end = stopTime != null ? stopTime : OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES);
		this.status = "COMPLETED";
		if (breakDuration != null) {
			this.breakDuration = breakDuration;
		}
		if (focusRating != null && focusRating > 0) {
			this.focusRating = focusRating;
		}
		if (outcomeSummary != null && !outcomeSummary.isBlank()) {
			this.outcomeSummary = outcomeSummary;
		}
		if (comment != null && !comment.isBlank()) {
			this.comment = comment;
		}
	}

	@Override
	public String toString() {
		return String.format(
				"Session [id=%s, status=%s, start=%s, end=%s, breakDuration=%s, duration=%s, netDuration=%s, focusRating=%s, comment=%s, outcomeSummary=%s]",
				id, status, start, end, breakDuration, duration, netDuration, focusRating, comment, outcomeSummary);
	}
}
