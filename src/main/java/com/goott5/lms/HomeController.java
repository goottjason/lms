package com.goott5.lms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 루트
    @GetMapping("/")
    public String home() {
        return "/layout/blank";
    }


}
