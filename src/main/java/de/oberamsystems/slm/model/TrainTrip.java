package de.oberamsystems.slm.model;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;

@Entity
public class TrainTrip {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String line;

	@ManyToOne
	private TrainStation origin;
	@ManyToOne
	private TrainStation destination;
	
	@Column(name = "starttime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
	@org.hibernate.annotations.Generated(event = { org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE })
	private Duration duration;
	
	public TrainTrip() {
	}
	
	public TrainTrip(Long id, String line, TrainStation origin, TrainStation destination, OffsetDateTime start, OffsetDateTime end,
			Duration duration) {
		this.id = id;
		this.line = line;
		this.origin = origin;
		this.destination = destination;
		this.start = start;
		this.end = end;
		this.duration = duration;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public TrainStation getOrigin() {
		return origin;
	}

	public void setOrigin(TrainStation origin) {
		this.origin = origin;
	}

	public TrainStation getDestination() {
		return destination;
	}

	public void setDestination(TrainStation destination) {
		this.destination = destination;
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
