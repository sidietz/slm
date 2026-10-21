package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sporttype")
public class SportType {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	
	public SportType() {
	}

	public SportType(Long id, String name) {
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("SportType [name=%s]", name);
	}
}
