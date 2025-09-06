package de.oberamsystems.slm.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Mood {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "tracked_at", columnDefinition = "DATE")
	private LocalDate date;
	private int happiness;
	private int impetus;
	private int stress;

	public Mood() {
	}

	public Mood(Long id, LocalDate date, int happiness, int impetus, int stress) {
		super();
		this.id = id;
		this.date = date;
		this.happiness = happiness;
		this.impetus = impetus;
		this.stress = stress;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public int getHappiness() {
		return happiness;
	}

	public void setHappiness(int happiness) {
		this.happiness = happiness;
	}

	public int getImpetus() {
		return impetus;
	}

	public void setImpetus(int impetus) {
		this.impetus = impetus;
	}

	public int getStress() {
		return stress;
	}

	public void setStress(int stress) {
		this.stress = stress;
	}

	public Long getId() {
		return id;
	}
}
