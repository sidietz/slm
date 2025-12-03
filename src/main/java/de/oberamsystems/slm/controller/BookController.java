package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.AuthorRepository;
import de.oberamsystems.slm.model.BookRepository;
import de.oberamsystems.slm.model.BookTypeEnum;
import de.oberamsystems.slm.model.Press;
import de.oberamsystems.slm.model.PressRepository;
import de.oberamsystems.slm.model.ReadingSession;
import de.oberamsystems.slm.model.ReadingSessionRepository;
import de.oberamsystems.slm.model.Book;
import de.oberamsystems.slm.model.BookGenreEnum;
import de.oberamsystems.slm.model.Author;


@Controller
public class BookController {

	Logger log = LoggerFactory.getLogger(BookController.class);
	
	@Autowired
	private BookRepository repo;
	
	@Autowired
	private AuthorRepository authorRepo;
	
	@Autowired
	private PressRepository pressRepo;
	
	@Autowired
	private ReadingSessionRepository readingSessionRepo;
	
	@GetMapping("/book")
	public String getbook(Model model) {
		List<Book> books = repo.findAll();
		model.addAttribute("books", books);
		return "book";
	}
	
	@GetMapping({"/author.html", "/author"})
	public String index(Model model) {
		List<Author> tss = authorRepo.findAll();
		model.addAttribute("authors", tss);
		return "author";
	}
	
	@GetMapping("/press")
	public String getPress(Model model) {
		model.addAttribute("presses", pressRepo.findAll());
		return "press";
	}
	
	@GetMapping("/readingsession")
	public String getReadingSession(Model model) {
		model.addAttribute("readingsessions", readingSessionRepo.findAllByOrderByStart());
		return "readingsession";
	}
	
	@GetMapping("/books")
	public String addBooks(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("books", repo.findAll());
		Book p = new Book();
		model.addAttribute("book", p);
		model.addAttribute("authors", authorRepo.findAll());
		model.addAttribute("presses", pressRepo.findAll());
		model.addAttribute("types", BookTypeEnum.values());
		model.addAttribute("genres", BookGenreEnum.values());
		return "books";
	}
	
	@PostMapping("/books")
	public String submitBooks(@ModelAttribute Book tt, Model model) {
		model.addAttribute("books", repo.findAll());
		model.addAttribute("authors", authorRepo.findAll());
		model.addAttribute("presses", pressRepo.findAll());
		model.addAttribute("types", BookTypeEnum.values());
		model.addAttribute("genres", BookGenreEnum.values());
		repo.save(tt);
		model.addAttribute("book", tt);
		return "redirect:/books";
	}
	
	@GetMapping("/authors")
	public String addAuthors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("authors", authorRepo.findAll());
		model.addAttribute("author", new Author());
		return "authors";
	}
	
	@PostMapping("/authors")
	public String submitAuthors(@ModelAttribute Author a, Model model) {
		model.addAttribute("authors", authorRepo.findAll());
		authorRepo.save(a);
		model.addAttribute("author", a);
		return "redirect:/authors";
	}
	
	@GetMapping("/reading-sessions")
	public String addReadingSessions(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("readingsessions", readingSessionRepo.findAllByOrderByStart());
		model.addAttribute("readingsession", new ReadingSession());
		model.addAttribute("books", repo.findAll());
		return "reading-sessions";
	}
	
	@PostMapping("/reading-sessions")
	public String submitReadingSessions(@ModelAttribute ReadingSession rs, Model model) {
		model.addAttribute("readingsessions", readingSessionRepo.findAllByOrderByStart());
		model.addAttribute("books", repo.findAll());
		Duration dur = Duration.between(rs.getStart(), rs.getEnd());
		long mins = dur.toMinutes();
		long pages = rs.getEndPageCount() - rs.getStartPageCount();
		float readingSpeed = ((float) pages / (float) mins) * 60.0F;
		rs.setReadingSpeed(readingSpeed);
		readingSessionRepo.save(rs);
		model.addAttribute("book", rs);
		model.addAttribute("readingsession", new ReadingSession());
		return "redirect:/reading-sessions";
	}
	
	@GetMapping("/add-book")
	public String addBook(@RequestParam(required = false) Long id, Model model) {
		Book p = new Book();
		model.addAttribute("book", p);
		model.addAttribute("authors", authorRepo.findAll());
		model.addAttribute("presses", pressRepo.findAll());
		model.addAttribute("types", BookTypeEnum.values());
		model.addAttribute("genres", BookGenreEnum.values());
		return "add-book";
	}
	
	@PostMapping("/add-book")
	public String submitBook(@ModelAttribute Book tt, Model model) {
		model.addAttribute("authors", authorRepo.findAll());
		model.addAttribute("presses", pressRepo.findAll());
		model.addAttribute("types", BookTypeEnum.values());
		model.addAttribute("genres", BookGenreEnum.values());
		repo.save(tt);
		model.addAttribute("book", tt);
		return "add-book";
	}
	
	@GetMapping("/add-reading-session")
	public String addReadingSession(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("readingsession", new ReadingSession());
		model.addAttribute("books", repo.findAll());
		return "add-reading-session";
	}
	
	@PostMapping("/add-reading-session")
	public String submitReadingSession(@ModelAttribute ReadingSession rs, Model model) {
		model.addAttribute("books", repo.findAll());
		Duration dur = Duration.between(rs.getStart(), rs.getEnd());
		long mins = dur.toMinutes();
		long pages = rs.getEndPageCount() - rs.getStartPageCount();
		float readingSpeed = ((float) pages / (float) mins) * 60.0F;
		rs.setReadingSpeed(readingSpeed);
		readingSessionRepo.save(rs);
		model.addAttribute("book", rs);
		model.addAttribute("readingsession", new ReadingSession());
		return "add-reading-session";
	}
	
	@GetMapping("/add-author")
	public String addAuthor(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("author", new Author());
		return "add-author";
	}
	
	@PostMapping("/add-author")
	public String submitAuthor(@ModelAttribute Author ts, Model model) {
		model.addAttribute("authors", authorRepo.findAll());
		authorRepo.save(ts);
		model.addAttribute("author", ts);
		return "add-author";
	}
	
	@GetMapping("/add-press")
	public String addPress(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("press", new Press());
		return "add-press";
	}
	
	@PostMapping("/add-press")
	public String submitPress(@ModelAttribute Press ts, Model model) {
		model.addAttribute("authors", authorRepo.findAll());
		pressRepo.save(ts);
		model.addAttribute("author", ts);
		return "add-press";
	}
}
