package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;
import org.hibernate.generator.EventType;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
public class Appointment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "doctor_id", referencedColumnName = "id")
	private Doctor doctor;
	@Column(name = "departure", columnDefinition = "TIMESTAMPTZ")
	private LocalDateTime departure;
	@Column(name = "arrival", columnDefinition = "TIMESTAMPTZ")
	private LocalDateTime arrival;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "travel_duration", columnDefinition = "interval", insertable = false, updatable = false)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	private Duration travelDuration;
	@Column(name = "starttime", columnDefinition = "TIMESTAMPTZ")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMPTZ")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	private Duration duration;
	@Column(name = "arrival_home", columnDefinition = "TIMESTAMPTZ")
	private LocalDateTime arrivalHome;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "complete_duration", columnDefinition = "interval", insertable = false, updatable = false)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	private Duration completeDuration;
	private String comment;
	
	public Appointment() {
	}
	
	public Appointment(Long id, Doctor doctor, LocalDateTime departure, LocalDateTime arrival, Duration travelDuration,
			LocalDateTime start, LocalDateTime end, Duration duration, LocalDateTime arrivalHome,
			Duration completeDuration, String comment) {
		super();
		this.id = id;
		this.doctor = doctor;
		this.departure = departure;
		this.arrival = arrival;
		this.travelDuration = travelDuration;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.arrivalHome = arrivalHome;
		this.completeDuration = completeDuration;
		this.comment = comment;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}

	public LocalDateTime getDeparture() {
		return departure;
	}

	public void setDeparture(LocalDateTime departure) {
		this.departure = departure;
	}

	public LocalDateTime getArrival() {
		return arrival;
	}

	public void setArrival(LocalDateTime arrival) {
		this.arrival = arrival;
	}

	public Duration getTravelDuration() {
		return travelDuration;
	}

	public void setTravelDuration(Duration travelDuration) {
		this.travelDuration = travelDuration;
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

	public LocalDateTime getArrivalHome() {
		return arrivalHome;
	}

	public void setArrivalHome(LocalDateTime arrivalHome) {
		this.arrivalHome = arrivalHome;
	}

	public Duration getCompleteDuration() {
		return completeDuration;
	}

	public void setCompleteDuration(Duration completeDuration) {
		this.completeDuration = completeDuration;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
