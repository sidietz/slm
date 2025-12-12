package de.oberamsystems.slm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HumanManagementController {

    @GetMapping("/human-management")
    public String index() {
        return "human-management";
    }
}
