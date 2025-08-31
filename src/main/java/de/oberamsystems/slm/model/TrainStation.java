package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
public class TrainStation {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String ds100;
	private String name;
	
	public TrainStation() {
	}
	
	public TrainStation(Long id, String ds100, String name) {
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
	
	
	
	
}
