package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Manufacturer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String hq;
	@Column(name = "founding_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate foundingDate;

	public Manufacturer() {
	}

	public Manufacturer(Long id, String name, String hq, LocalDate foundingDate) {
		super();
		this.id = id;
		this.name = name;
		this.hq = hq;
		this.foundingDate = foundingDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHq() {
		return hq;
	}

	public void setHq(String hq) {
		this.hq = hq;
	}

	public LocalDate getFoundingDate() {
		return foundingDate;
	}

	public void setFoundingDate(LocalDate foundingDate) {
		this.foundingDate = foundingDate;
	}

	public Long getId() {
		return id;
	}
}
