package com.goott5.lms.coursemodify.controller;

import com.goott5.lms.coursemodify.domain.CourseModifyDTO;
import com.goott5.lms.coursemodify.service.CourseModifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courseManagement/courseModify")
@RequiredArgsConstructor
@Slf4j
public class CourseModifyController {

  private final CourseModifyService courseModifyService;

  @GetMapping("")
  public String courseModify(@RequestParam(value = "courseId", defaultValue = "-1") Integer courseId, Model model) {

    if(courseId == -1) {
      return "redirect:/courseManagement/courseList";
    }

    model.addAttribute("course", courseModifyService.getDetailByCourseId(courseId));


    return "courseManagement/courseModify";

  }

  @PostMapping("/modifyCourse")
  @ResponseBody
  public String modifyCourse(@RequestBody CourseModifyDTO courseModifyDTO, RedirectAttributes redirectAttributes) {

//    log.info("Modify Course {}", courseModifyDTO);
    boolean isSuccess = courseModifyService.modifyCourse(courseModifyDTO);
    return isSuccess ? "success" : "fail";
  }

  @GetMapping("/modifySuccess")
  public String modifySuccess(@RequestParam boolean isSuccess, @RequestParam Integer courseId, RedirectAttributes redirectAttributes) {

    redirectAttributes.addFlashAttribute("isSaveSuccess", isSuccess);
    return "redirect:/courseManagement/courseDetail?courseId=" + courseId;
  }

}
