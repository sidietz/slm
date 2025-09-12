package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Game {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String series;
	@ManyToOne
	@JoinColumn(name = "publisher_id", referencedColumnName = "id")
	private Publisher publisher;
	@ManyToOne
	@JoinColumn(name = "studio_id", referencedColumnName = "id")
	private Studio studio;
	private float price;
	@Column(name = "release_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate releaseDate;
	@Column(name = "last_played", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate lastPlayed;

	public Game() {
	}

	public Game(Long id, String title, String series, Studio studio, Publisher publisher, float price,
			LocalDate releaseDate, LocalDate lastPlayed) {
		super();
		this.id = id;
		this.title = title;
		this.series = series;
		this.studio = studio;
		this.publisher = publisher;
		this.price = price;
		this.releaseDate = releaseDate;
		this.lastPlayed = lastPlayed;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public Studio getStudio() {
		return studio;
	}

	public void setStudio(Studio studio) {
		this.studio = studio;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public LocalDate getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	public LocalDate getLastPlayed() {
		return lastPlayed;
	}

	public void setLastPlayed(LocalDate lastPlayed) {
		this.lastPlayed = lastPlayed;
	}

	public Long getId() {
		return id;
	}
}
