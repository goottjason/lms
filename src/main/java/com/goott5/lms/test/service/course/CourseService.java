package com.goott5.lms.test.service.course;

import com.goott5.lms.test.domain.course.CourseInfoDTO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public interface CourseService {

  // 관리자 강좌 필터
  List<String> getCourseListForAdmin(Boolean isInProgress);

  // 수강생, 강사 강좌 필터
  List<CourseInfoDTO> getCourseListForUser(HttpSession session);
}
