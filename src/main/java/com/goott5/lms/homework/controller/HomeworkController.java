package com.goott5.lms.homework.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeworkController {

    @GetMapping("/homeworkList")
    public String homeworkList() {
        return "/homework/homeworkList";
    }
}
