package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
@Table(name = "meditationsession")
public class MeditationSession {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;
	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable=false)
	private Duration duration;
	
	public MeditationSession() {
	}

	public MeditationSession(long id, LocalDateTime start, LocalDateTime end, String comment) {
		this.id = id;
		this.start = start;
		this.end = end;
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

	public long getId() {
		return id;
	}

	public Duration getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return String.format("SportSession [id=%s, start=%s, end=%s, duration=%s]", id, start, end, duration);
	}
	
	
	

}
