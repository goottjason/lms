package com.goott5.lms.courseboardqna.controller;

import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

  @GetMapping("/detail/{boardNo}")
  public String qnaDetail(
      @PathVariable int boardNo,
      HttpSession session,
      Model model
  ) {

    model.addAttribute("boardNo", boardNo);
    model.addAttribute("loginId", ((UserVO) session.getAttribute("loginUser")).getLoginId());
    return "courseBoardQnA/qnaDetail";
  }

  @GetMapping("/modify/{boardNo}")
  public String qnaModify(@PathVariable int boardNo,
      Model model) {

    model.addAttribute("boardNo", boardNo);
    return "courseBoardQnA/qnaModify";
  }
}
