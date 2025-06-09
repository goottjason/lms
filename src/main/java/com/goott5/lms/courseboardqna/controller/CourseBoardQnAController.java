package com.goott5.lms.courseboardqna.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CourseBoardQnAController {

    @GetMapping("/qnaList")
    public String qnaList() {
        return "/courseBoardQnA/qnaList";
    }
    @GetMapping("/qnaRegister")
    public String qnaRegister() {
        return "/courseBoardQnA/qnaRegister";
    }
    @GetMapping("/qnaDetail")
    public String qnaDetail() {
        return "/courseBoardQnA/qnaDetail";
    }
    @GetMapping("/qnaModify")
    public String qnaModify() {
        return "/courseBoardQnA/qnaModify";
    }
}
