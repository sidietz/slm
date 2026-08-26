package de.oberamsystems.slm.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "point")
public class Point {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	private String latitude;
	private String longitude;
	@OneToMany(mappedBy="start")
	private List<Route> starts = new ArrayList<Route>();
	@OneToMany(mappedBy="end")
	private List<Route> ends = new ArrayList<Route>();

	public Point() {
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

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public List<Route> getStarts() {
		return starts;
	}

	public void setStarts(List<Route> starts) {
		this.starts = starts;
	}

	public List<Route> getEnds() {
		return ends;
	}

	public void setEnds(List<Route> ends) {
		this.ends = ends;
	}

	public Long getId() {
		return id;
	}
	
	
}
