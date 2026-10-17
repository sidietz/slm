package de.oberamsystems.slm.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "reading_session")
@DiscriminatorValue("READING")
public class ReadingSession extends Session {

	@ManyToOne
	@JoinColumn(name = "book_id", referencedColumnName = "id")
	private Book book;

	@Column(name = "start_page_count")
	private int startPageCount;

	@Column(name = "end_page_count")
	private int endPageCount;

	@Column(name = "reading_speed")
	private float readingSpeed;

	@OneToMany
	private Set<Book> books = new HashSet<Book>();

	public ReadingSession() {
		super();
	}

	public ReadingSession(Book book, int startPageCount, int endPageCount, float readingSpeed, Set<Book> books) {
		super();
		this.book = book;
		this.startPageCount = startPageCount;
		this.endPageCount = endPageCount;
		this.readingSpeed = readingSpeed;
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

	public Set<Book> getBooks() {
		return books;
	}

	public void setBooks(Set<Book> books) {
		this.books = books;
	}

	@Override
	public String toString() {
		return String.format(
				"ReadingSession [id=%s, book=%s, startPageCount=%s, endPageCount=%s, readingSpeed=%s, books=%s, session=%s]",
				getId(), book, startPageCount, endPageCount, readingSpeed, books, super.toString());
	}
}
