package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Gratitude {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "created_at", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;
	private String description;
	
	public Gratitude() {
	}

	public Gratitude(Long id, LocalDate date, String description) {
		super();
		this.id = id;
		this.date = date;
		this.description = description;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}
}
