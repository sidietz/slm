package de.oberamsystems.slm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MiscManagementController {

    @GetMapping("/misc-management")
    public String index() {
        return "misc-management";
    }
}
