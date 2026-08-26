package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class BookControllerTest {

    @Test
    public void testBooks() throws Exception {
        BookController controller = new BookController();
        
        BookRepository bookRepo = mock(BookRepository.class);
        AuthorRepository authorRepo = mock(AuthorRepository.class);
        PressRepository pressRepo = mock(PressRepository.class);
        ReadingSessionRepository readingRepo = mock(ReadingSessionRepository.class);
        
        java.lang.reflect.Field f1 = BookController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, bookRepo);
        
        java.lang.reflect.Field f2 = BookController.class.getDeclaredField("authorRepo");
        f2.setAccessible(true);
        f2.set(controller, authorRepo);
        
        java.lang.reflect.Field f3 = BookController.class.getDeclaredField("pressRepo");
        f3.setAccessible(true);
        f3.set(controller, pressRepo);
        
        java.lang.reflect.Field f4 = BookController.class.getDeclaredField("readingSessionRepo");
        f4.setAccessible(true);
        f4.set(controller, readingRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // GET /books
        String view = controller.addBooks(1L, model);
        assertEquals("books", view);
        
        // POST /books
        Book book = new Book();
        String view2 = controller.submitBooks(book, model);
        assertEquals("redirect:/books", view2);
        
        // GET /authors
        String view3 = controller.addAuthors(1L, model);
        assertEquals("authors", view3);
        
        // POST /authors
        Author a = new Author();
        String view4 = controller.submitAuthors(a, model);
        assertEquals("redirect:/authors", view4);
        
        // GET /reading-sessions
        String view5 = controller.addReadingSessions(1L, model);
        assertEquals("reading-sessions", view5);
        
        // POST /reading-sessions
        ReadingSession rs = new ReadingSession();
        rs.setStart(LocalDateTime.now().minusHours(1));
        rs.setEnd(LocalDateTime.now());
        rs.setStartPageCount(10);
        rs.setEndPageCount(60);
        String view6 = controller.submitReadingSessions(rs, model);
        assertEquals("redirect:/reading-sessions", view6);
        assertEquals(50.0f, rs.getReadingSpeed());
    }
}
