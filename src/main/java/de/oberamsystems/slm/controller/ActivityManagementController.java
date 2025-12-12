package de.oberamsystems.slm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActivityManagementController {

    @GetMapping("/activity-management")
    public String index() {
        return "activity-management";
    }
}
