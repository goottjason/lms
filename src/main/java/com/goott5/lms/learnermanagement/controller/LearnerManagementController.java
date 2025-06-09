package com.goott5.lms.learnermanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LearnerManagementController {

    @GetMapping("/learnerList")
    public String learnerList() {
        return "/learnerManagement/learnerList";
    }
}
