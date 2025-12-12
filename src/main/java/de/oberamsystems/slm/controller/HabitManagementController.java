package de.oberamsystems.slm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HabitManagementController {

    @GetMapping("/habit-management")
    public String index() {
        return "habit-management";
    }
}
