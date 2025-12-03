package de.oberamsystems.slm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookManagementController {

    @GetMapping("/book-management")
    public String index() {
        return "book-management";
    }
}
