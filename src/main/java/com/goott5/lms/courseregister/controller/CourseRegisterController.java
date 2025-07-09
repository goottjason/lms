package com.goott5.lms.courseregister.controller;

import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.courseregister.domain.ClassroomVO;
import com.goott5.lms.courseregister.domain.CourseSaveDTO;
import com.goott5.lms.courseregister.domain.UserVO;
import com.goott5.lms.courseregister.service.CourseRegisterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

  @GetMapping("/getCancelDates")
  @ResponseBody
  public List<CancelDateVO> getCancelDates() {

    return courseRegisterService.getCancelDates();
  }

  @GetMapping("/getClassroom")
  @ResponseBody
  public List<ClassroomVO> getClassroom() {

    return courseRegisterService.getClassroom();
  }

  @PostMapping("/saveCourse")
  @ResponseBody
  public String saveCourse(@RequestBody CourseSaveDTO courseSaveDTO) {

//    log.info("courseSaveDTO = {}", courseSaveDTO);
    boolean isSuccess = courseRegisterService.saveCourse(courseSaveDTO);

    return isSuccess ? "success" : "fail";
  }

  @GetMapping("/checkNameDuplicate")
  @ResponseBody
  public String checkNameDuplicate(String name) {

//    log.info("name = {}", name);
    boolean isDuplicate = courseRegisterService.checkNameDuplicate(name);
    if(isDuplicate) {
      return "duplicateName";
    } else {
      return "availableName";
    }
  }
  @GetMapping("saveSuccess")
  public String saveSuccess(@RequestParam boolean isSuccess, RedirectAttributes redirectAttributes) {

    redirectAttributes.addFlashAttribute("isSaveSuccess", isSuccess);
    return "redirect:/courseManagement/courseList";
  }


}
