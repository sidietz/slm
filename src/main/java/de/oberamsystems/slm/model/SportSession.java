package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
@Table(name = "sportsession")
public class SportSession {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;
	@ManyToOne
	@JoinColumn(name="type", referencedColumnName = "id")
	private SportType type;
	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable=false)
	private Duration duration;
	
	private String comment;
	
	public SportSession() {
	}

	public SportSession(long id, SportType type, LocalDateTime start, LocalDateTime end, String comment) {
		this.id = id;
		this.type = type;
		this.start = start;
		this.end = end;
		this.comment = comment;
	}

	public SportType getType() {
		return type;
	}

	public void setType(SportType type) {
		this.type = type;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getId() {
		return id;
	}

	public Duration getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return String.format("SportSession [id=%s, type=%s, start=%s, end=%s, duration=%s, comment=%s]", id, type,
				start, end, duration, comment);
	}
}
