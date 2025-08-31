package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
public class Activity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name="type", referencedColumnName = "id")
	private ActivityType type;
	private String title;
	private String location;
	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable=false)
	private Duration duration;

	public Activity() {
	}

	public Activity(Long id, ActivityType type, String title, String location, LocalDateTime start, LocalDateTime end, Duration duration) {
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

	public Long getId() {
		return id;
	}
}
