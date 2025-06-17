package com.goott5.lms.test.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/test")
public class TestViewController {

  @GetMapping("/testList")
  public String getTestListPage() {
    return "test/testList";
  }

  @GetMapping("/register")
  public String getTestRegisterPage(
          @RequestParam(name = "currentPageNo", required = false, defaultValue = "1") int pageNo,
          Model model) {
    model.addAttribute("pageNo", pageNo);
    return "test/testRegister";
  }

  @GetMapping("/testDetail/{testId}")
  public String getTestDetailPage(@PathVariable int testId) {
    return "test/testDetail";
  }

}
