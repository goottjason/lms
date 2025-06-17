package com.goott5.lms.test.controller.course;

import com.goott5.lms.test.domain.apiresponse.ApiResponse;
import com.goott5.lms.test.domain.course.CourseInfoDTO;
import com.goott5.lms.test.service.course.CourseService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestCourseController {

  private final CourseService courseService;

  @GetMapping("/admin/courses")
  public ResponseEntity<ApiResponse<List<String>>> getCourseListForAdmin(
      @RequestParam(required = false) Boolean isInProgress) {

    List<String> courseList = courseService.getCourseListForAdmin(isInProgress);
    return ApiResponse.respondOk(200, "SUCCESS", courseList);
  }

  @GetMapping("/courses")
  public ResponseEntity<ApiResponse<List<CourseInfoDTO>>> getCourseListForUser(
      HttpSession session) {

    List<CourseInfoDTO> courseList = courseService.getCourseListForUser(session);

    return ApiResponse.respondOk(200, "SUCCESS", courseList);
  }

}
