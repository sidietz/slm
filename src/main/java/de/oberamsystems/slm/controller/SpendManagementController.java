package de.oberamsystems.slm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpendManagementController {

    @GetMapping("/spend-management")
    public String index() {
        return "spend-management";
    }
}
