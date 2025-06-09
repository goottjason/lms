package com.goott5.lms.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class testController {

    @GetMapping("/testList")
    public String testList() {
        return "/test/testList";
    }
}
