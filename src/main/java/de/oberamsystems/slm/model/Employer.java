package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employer")
public class Employer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", columnDefinition = "TEXT", nullable = false)
	private String name;

	public Employer() {
	}

	public Employer(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Employer(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("Employer [id=%s, name=%s]", id, name);
	}
}
