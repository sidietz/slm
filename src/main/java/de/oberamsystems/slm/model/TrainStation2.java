package de.oberamsystems.slm.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "trainstation2")
public class TrainStation2 {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String ds100;
	private String name;
	@ManyToMany(mappedBy = "stations")
	private Set<TrainLine> lines = new HashSet<>();

	public TrainStation2() {
	}
	
	public TrainStation2(String ds100, String name) {
		this.ds100 = ds100;
		this.name = name;
	}

	public TrainStation2(Long id, String ds100, String name) {
		this.id = id;
		this.ds100 = ds100;
		this.name = name;
	}

	public String getDs100() {
		return ds100;
	}

	public void setDs100(String ds100) {
		this.ds100 = ds100;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void addLine(TrainLine line) {
		this.lines.add(line);
		line.getStations().add(this);
	}

	public void removeLine(TrainLine line) {
		this.lines.remove(line);
		line.getStations().remove(this);
	}
}
