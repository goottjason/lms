package com.goott5.lms.instructorhome.controller;

import com.goott5.lms.instructorhome.domain.HomeRequestDTO;
import com.goott5.lms.instructorhome.domain.HomeResponseDTO;
import com.goott5.lms.instructorhome.service.InstructorHomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/home/instructorHome")
public class InstructorHomeController {

  private final InstructorHomeService instructorHomeService;

  @GetMapping("")
  public String instructorHome() {

    return "home/instructorHome";

  }

  @GetMapping("/getHomeData")
  @ResponseBody
  public HomeResponseDTO getHomeData(HomeRequestDTO homeRequestDTO) {

    HomeResponseDTO homeResponseDTO = instructorHomeService.getHomeData(homeRequestDTO.getCourseId());

    return homeResponseDTO;

  }

}
