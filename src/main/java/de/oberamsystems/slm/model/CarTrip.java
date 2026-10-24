package de.oberamsystems.slm.model;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;
import org.hibernate.generator.EventType;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;

@Entity
public class CarTrip {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "car_id", referencedColumnName = "id")
	private Car car;
	
	@ManyToOne
	private Route route;
	
	@Column(name = "is_way_back")
	private boolean isWayBack;
	
	@Column(name = "starttime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	private Duration duration;
	
	public CarTrip() {
	}

	public CarTrip(Long id, Route route, boolean isWayBack, OffsetDateTime start, OffsetDateTime end, Duration duration) {
		super();
		this.id = id;
		this.route = route;
		this.isWayBack = isWayBack;
		this.start = start;
		this.end = end;
		this.duration = duration;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public boolean isWayBack() {
		return isWayBack;
	}

	public void setWayBack(boolean isWayBack) {
		this.isWayBack = isWayBack;
	}
	
	public void setIsWayBack(boolean isWayBack) {
		this.isWayBack = isWayBack;
	}
	
	public boolean getIsWayBack() {
		return isWayBack;
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
