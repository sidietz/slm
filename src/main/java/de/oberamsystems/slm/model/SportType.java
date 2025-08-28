package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sporttype")
public class SportType {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name;
	
	public SportType() {
	}

	public SportType(long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}
	
	

}
