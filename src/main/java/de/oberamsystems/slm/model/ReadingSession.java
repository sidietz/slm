package de.oberamsystems.slm.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;

@Entity
@Table(name = "readingsession")
public class ReadingSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name="book_id", referencedColumnName = "id")
	private Book book;
	private int startPageCount;
	private int endPageCount;
	private float readingSpeed;
	@Column(name = "starttime", columnDefinition = "TIMESTAMP")
	private LocalDateTime start;
	@Column(name = "endtime", columnDefinition = "TIMESTAMP")
	private LocalDateTime end;
	@Type(PostgreSQLIntervalType.class)
	@Column(name = "duration", columnDefinition = "interval", insertable=false)
	private Duration duration;
	
	@OneToMany
	private Set<Book> books = new HashSet<Book>();

	public ReadingSession() {
	}

	public ReadingSession(Long id, Book book, int startPageCount, int endPageCount, float readingSpeed,
			LocalDateTime start, LocalDateTime end, Duration duration, Set<Book> books) {
		super();
		this.id = id;
		this.book = book;
		this.startPageCount = startPageCount;
		this.endPageCount = endPageCount;
		this.readingSpeed = readingSpeed;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.books = books;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public int getStartPageCount() {
		return startPageCount;
	}

	public void setStartPageCount(int startPageCount) {
		this.startPageCount = startPageCount;
	}

	public int getEndPageCount() {
		return endPageCount;
	}

	public void setEndPageCount(int endPageCount) {
		this.endPageCount = endPageCount;
	}

	public float getReadingSpeed() {
		return readingSpeed;
	}

	public void setReadingSpeed(float readingSpeed) {
		this.readingSpeed = readingSpeed;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public Set<Book> getBooks() {
		return books;
	}

	public void setBooks(Set<Book> books) {
		this.books = books;
	}

	public Long getId() {
		return id;
	}
}
