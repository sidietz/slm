package de.oberamsystems.slm.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Studio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String hq;
	@Column(name = "founding_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate foundingDate;
	
	@OneToMany
	private Set<Game> games = new HashSet<Game>();

	public Studio() {
	}

	public Studio(Long id, String name, String hq, LocalDate foundingDate, Set<Game> games) {
		super();
		this.id = id;
		this.name = name;
		this.hq = hq;
		this.foundingDate = foundingDate;
		this.games = games;
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

	public Set<Game> getGames() {
		return games;
	}

	public void setGames(Set<Game> games) {
		this.games = games;
	}

	public Long getId() {
		return id;
	}
}
