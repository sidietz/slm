package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
public class Speciality {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	
	public Speciality() {
	}

	public Speciality(Long id, String title) {
		super();
		this.id = id;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("Speciality [id=%s, title=%s]", id, title);
	}
	
}
