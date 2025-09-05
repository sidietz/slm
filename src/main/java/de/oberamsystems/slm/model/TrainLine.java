package de.oberamsystems.slm.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "trainline")
public class TrainLine {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	@ManyToMany
	@JoinTable(name = "line_station", joinColumns = @JoinColumn(name = "line_id"), inverseJoinColumns = @JoinColumn(name = "station_id"))
	private Set<TrainStation2> stations = new HashSet<>();
	
	public TrainLine() {
	}

	public TrainLine(Long id, String name, String description, Set<TrainStation2> stations) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.stations = stations;
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

	public Set<TrainStation2> getStations() {
		return stations;
	}

	public void setStations(Set<TrainStation2> stations) {
		this.stations = stations;
	}

	public Long getId() {
		return id;
	}
	
	
	
}
