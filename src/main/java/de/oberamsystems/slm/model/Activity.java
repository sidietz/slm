package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
public class Activity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "type", referencedColumnName = "id")
	private ActivityType type;
	private String title;
	private String location;
	@Column(name = "starttime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
	@org.hibernate.annotations.Generated(event = { org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE })
	private Duration duration;

	public Activity() {
	}

	public Activity(Long id, ActivityType type, String title, String location, OffsetDateTime start, OffsetDateTime end, Duration duration) {
		this.id = id;
		this.type = type;
		this.title = title;
		this.location = location;
		this.start = start;
		this.end = end;
		this.duration = duration;
	}

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
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

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
