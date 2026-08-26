package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "route")
public class Route {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	@ManyToOne
	@JoinColumn(name="start_point", referencedColumnName="id")
	private Point start;
	@JoinColumn(name="end_point", referencedColumnName="id")
	@ManyToOne
	private Point end;
	private Double airDistance;
	private Double driveDistance;
	
	public Route() {
	}

	public Route(Long id, String name, String description, Point start, Point end, Double airDistance,
			Double driveDistance) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.start = start;
		this.end = end;
		this.airDistance = airDistance;
		this.driveDistance = driveDistance;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Point getStart() {
		return start;
	}

	public void setStart(Point start) {
		this.start = start;
	}

	public Point getEnd() {
		return end;
	}

	public void setEnd(Point end) {
		this.end = end;
	}

	public Double getAirDistance() {
		return airDistance;
	}

	public void setAirDistance(Double airDistance) {
		this.airDistance = airDistance;
	}

	public Double getDriveDistance() {
		return driveDistance;
	}

	public void setDriveDistance(Double driveDistance) {
		this.driveDistance = driveDistance;
	}

	public Long getId() {
		return id;
	}
	
	
	
	
}
