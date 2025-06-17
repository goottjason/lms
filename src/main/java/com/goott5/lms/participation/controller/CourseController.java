package com.goott5.lms.participation.controller;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.mapper.CourseMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 과정(Course) 정보 조회 컨트롤러
 */
@Controller
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

  private final CourseMapper courseMapper;

  /**
   * 과정 정보 조회 API
   */
  @GetMapping("/{courseId}")
  @ResponseBody
  public ResponseEntity<?> getCourseInfo(@PathVariable Integer courseId) {
    try {
      CourseVO course = courseMapper.selectCourseById(courseId);

      if (course != null) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "course", course
        ));
      } else {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "존재하지 않는 과정입니다."
        ));
      }

    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "과정 조회에 실패했습니다."
      ));
    }
  }
}
