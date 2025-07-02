package com.goott5.lms.test.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class LearnerMainViewController {

  @GetMapping("/learnerHome")
  public String getTestHomePage() {
    return "test/learnerHome";
  }

}
