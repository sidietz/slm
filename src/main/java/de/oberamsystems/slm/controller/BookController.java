package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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


import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	
	@GetMapping("/books")
	public String addBooks(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("books", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		Book p = new Book();
		model.addAttribute("book", p);
		model.addAttribute("authors", authorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		model.addAttribute("presses", pressRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("types", BookTypeEnum.values());
		model.addAttribute("genres", BookGenreEnum.values());
		return "books";
	}
	
	public String submitBooks(Book tt, Model model) {
		return submitBooks(tt, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/books")
	public String submitBooks(@ModelAttribute Book tt, Model model, RedirectAttributes redirectAttributes) {
		if (tt.getAuthor() == null || tt.getAuthor().getId() == null || !authorRepo.existsById(tt.getAuthor().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Author is required.");
			return "redirect:/books";
		}
		if (tt.getPress() == null || tt.getPress().getId() == null || !pressRepo.existsById(tt.getPress().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Press / Imprint is required.");
			return "redirect:/books";
		}
		if (tt.getIsbn() == null || tt.getIsbn().isBlank()) {
			redirectAttributes.addFlashAttribute("errorMessage", "ISBN is required.");
			return "redirect:/books";
		}
		if (tt.getId() != null && (tt.getId() <= 0 || !repo.existsById(tt.getId()))) {
			tt.setId(null);
		}
		repo.save(tt);
		return "redirect:/books";
	}
	
	@GetMapping("/authors")
	public String addAuthors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("authors", authorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		model.addAttribute("author", new Author());
		return "authors";
	}
	
	@PostMapping("/authors")
	public String submitAuthors(@ModelAttribute Author a, Model model) {
		model.addAttribute("authors", authorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		if (a.getId() != null && (a.getId() <= 0 || !authorRepo.existsById(a.getId()))) {
			a.setId(null);
		}
		authorRepo.save(a);
		model.addAttribute("author", a);
		return "redirect:/authors";
	}

	@GetMapping("/press")
	public String addPresses(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("presses", pressRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("press", new Press());
		return "press";
	}

	@PostMapping("/press")
	public String submitPresses(@ModelAttribute Press p, Model model) {
		model.addAttribute("presses", pressRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (p.getId() != null && (p.getId() <= 0 || !pressRepo.existsById(p.getId()))) {
			p.setId(null);
		}
		pressRepo.save(p);
		model.addAttribute("press", p);
		return "redirect:/press";
	}
	
	@GetMapping("/reading-sessions")
	public String addReadingSessions(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("readingsessions", readingSessionRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("readingsession", new ReadingSession());
		model.addAttribute("books", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		return "reading-sessions";
	}
	
	public String submitReadingSessions(ReadingSession rs, Model model) {
		return submitReadingSessions(rs, 0L, model);
	}

	@PostMapping("/reading-sessions")
	public String submitReadingSessions(@ModelAttribute ReadingSession rs,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes,
			Model model) {
		model.addAttribute("readingsessions", readingSessionRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("books", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		if (breakMinutes != null && breakMinutes > 0) {
			rs.setBreakDuration(Duration.ofMinutes(breakMinutes));
		}
		if (rs.getStart() != null && rs.getEnd() != null) {
			Duration dur = Duration.between(rs.getStart(), rs.getEnd());
			long netMins = Math.max(1, dur.toMinutes() - (breakMinutes != null ? breakMinutes : 0));
			long pages = rs.getEndPageCount() - rs.getStartPageCount();
			if (pages > 0) {
				float readingSpeed = ((float) pages / (float) netMins) * 60.0F;
				rs.setReadingSpeed(readingSpeed);
			}
		}
		readingSessionRepo.save(rs);
		model.addAttribute("readingsession", new ReadingSession());
		return "redirect:/reading-sessions";
	}
}
