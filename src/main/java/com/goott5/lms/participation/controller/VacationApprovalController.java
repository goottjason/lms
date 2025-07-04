package com.goott5.lms.participation.controller;

import com.goott5.lms.participation.service.VacationService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/learnerManagement")
@RequiredArgsConstructor
@Slf4j
public class VacationApprovalController {

  private final VacationService vacationService;

  // ========== 페이지 렌더링 ==========

  /**
   * 휴가 승인 페이지
   */
  @GetMapping("/vacationApproval")
  public String vacationApprovalPage(Model model, HttpSession session) {
    try {
      UserVO loginUser = (UserVO) session.getAttribute("loginUser");


      // 강사가 맡은 과정 목록 조회
      Map<String, Object> instructorCourses = vacationService.getInstructorCourses(loginUser.getId());

      // 선택된 과정 ID 결정 (기본값: 현재 과정)
      Map<String, Object> currentCourse = (Map<String, Object>) instructorCourses.get("currentCourse");
      Integer selectedCourseId = currentCourse != null ? (Integer) currentCourse.get("id") : null;
      boolean isCurrentCourse = true;

      model.addAttribute("loginUser", loginUser);
      model.addAttribute("instructorCourses", instructorCourses);
      model.addAttribute("selectedCourseId", selectedCourseId);
      model.addAttribute("isCurrentCourse", isCurrentCourse);
      model.addAttribute("currentPage", 0);

      return "participation/vacationApproval";

    } catch (Exception e) {
      log.error("휴가 승인 페이지 로드 중 오류 발생", e);
      model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
      return "participation/vacationApproval";
    }
  }
}
