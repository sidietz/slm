package de.oberamsystems.slm.model;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;

@Entity
public class TrainTrip2 {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private TrainLine line;

	@ManyToOne
	private TrainStation origin;
	@ManyToOne
	private TrainStation destination;
	
	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable=false)
	private Duration duration;
	
	public TrainTrip2() {
	}
	
	public TrainTrip2(Long id, TrainLine line, TrainStation origin, TrainStation destination, LocalDateTime start, LocalDateTime end,
			Duration duration) {
		this.id = id;
		this.line = line;
		this.origin = origin;
		this.destination = destination;
		this.start = start;
		this.end = end;
		this.duration = duration;
	}

	public TrainLine getLine() {
		return line;
	}

	public void setLine(TrainLine line) {
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
