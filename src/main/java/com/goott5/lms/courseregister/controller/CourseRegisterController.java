package com.goott5.lms.courseregister.controller;

import com.goott5.lms.courseregister.domain.UserVO;
import com.goott5.lms.courseregister.service.CourseRegisterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/courseRegister")
@RequiredArgsConstructor
@Slf4j
public class CourseRegisterController {

  private final CourseRegisterService courseRegisterService;

  @GetMapping("")
  public String courseRegister() {
    return "/courseManagement/courseRegister";
  }

  @GetMapping("/getNotAssignmentInstructor")
  @ResponseBody
  public List<UserVO> getNotAssignmentInstructor() {

    return courseRegisterService.getNotAssignmentInstructor();

  }

  @GetMapping("/getCourseHead")
  @ResponseBody
  public List<UserVO> getCourseHead() {

    return courseRegisterService.getCourseHead();
  }


}
