package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	 @Column(nullable=false, unique=true, length=13) 
	private String isbn;
	private String title;
	private String series;
	private int pageCount;
	private float price;
	@Column(name = "buy_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate buyDate;
	@Column(name = "last_read", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate lastRead;
	@ManyToOne
	@JoinColumn(name="author_id", referencedColumnName = "id")
	private Author author;
	@ManyToOne
	@JoinColumn(name="press_id", referencedColumnName = "id")
	private Press press;
	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	private BookTypeEnum bookType; // fiction/non-fiction
	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	private BookGenreEnum bookGenre; // see compiled list

	public Book() {
	}

	public Book(Long id, String isbn, String title, String series, int pageCount, float price, LocalDate buyDate,
			LocalDate lastRead, Author author, Press press, BookTypeEnum bookType, BookGenreEnum bookGenre) {
		super();
		this.id = id;
		this.isbn = isbn;
		this.title = title;
		this.series = series;
		this.pageCount = pageCount;
		this.price = price;
		this.buyDate = buyDate;
		this.lastRead = lastRead;
		this.author = author;
		this.press = press;
		this.bookType = bookType;
		this.bookGenre = bookGenre;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
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

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public LocalDate getBuyDate() {
		return buyDate;
	}

	public void setBuyDate(LocalDate buyDate) {
		this.buyDate = buyDate;
	}

	public LocalDate getLastRead() {
		return lastRead;
	}

	public void setLastRead(LocalDate lastRead) {
		this.lastRead = lastRead;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public Press getPress() {
		return press;
	}

	public void setPress(Press press) {
		this.press = press;
	}

	public BookTypeEnum getBookType() {
		return bookType;
	}

	public void setBookType(BookTypeEnum bookType) {
		this.bookType = bookType;
	}

	public BookGenreEnum getBookGenre() {
		return bookGenre;
	}

	public void setBookGenre(BookGenreEnum bookGenre) {
		this.bookGenre = bookGenre;
	}

	public Long getId() {
		return id;
	}
}
