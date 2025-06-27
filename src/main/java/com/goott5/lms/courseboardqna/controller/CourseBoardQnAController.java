package com.goott5.lms.courseboardqna.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/courseBoardQnA")
public class CourseBoardQnAController {

  @GetMapping("/list")
  public String qnaList() {
    return "courseBoardQnA/qnaList";
  }

  @GetMapping("/register")
  public String qnaRegister() {
    return "courseBoardQnA/qnaRegister";
  }

  @GetMapping("/detail")
  public String qnaDetail() {
    return "courseBoardQnA/qnaDetail";
  }

  @GetMapping("/modify")
  public String qnaModify() {
    return "courseBoardQnA/qnaModify";
  }
}
