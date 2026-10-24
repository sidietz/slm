package de.oberamsystems.slm.model;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;

@Entity
@Table(name = "calendar_event")
public class CalendarEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private String description;
	@Column(name = "starttime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
	@org.hibernate.annotations.Generated(event = { org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE })
	private Duration duration;
	
	public CalendarEvent() {
	}

	public CalendarEvent(Long id, String title, String description, OffsetDateTime start, OffsetDateTime end) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.start = start;
		this.end = end;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	
	public Duration getDuration() {
		return duration;
	}

	public void setId(Long id) {
		this.id = id;
	}
}