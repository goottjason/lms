package com.goott5.lms.participation.controller;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.service.AttendanceService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.websocket.RemoteEndpoint.Async;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 출결(participation) 관련 웹 컨트롤러
 */
@Controller
@Slf4j
@RequestMapping("/participation")
@RequiredArgsConstructor
public class ParticipationController {

  private final AttendanceService attendanceService;
  private final ParticipationCourseMapper participationCourseMapper;

  /**
   * 출결 조회 메인 페이지 (세션에서 로그인 사용자 정보 가져오기)
   */
  @GetMapping("/participationView")
  public String participationView(Model model, HttpSession session) {
    try {
      // 세션에서 로그인 사용자 정보 가져오기
      // UserVO로 캐스팅 변경
      UserVO loginUser = (UserVO) session.getAttribute("loginUser");
      if (loginUser == null) {
        log.warn("로그인 정보가 세션에 없습니다.");
        return "redirect:/login";
      }

      Integer userId = loginUser.getId(); // UserVO의 getId() 메서드 사용
      log.info("로그인한 사용자 ID: {}", userId);

      // 사용자의 learnerEnrollmentId 조회
      Integer learnerEnrollmentId = attendanceService.getLearnerEnrollmentIdByUserId(userId);
      if (learnerEnrollmentId == null) {
        log.warn("해당 사용자는 수강 중인 과정이 없습니다: userId={}", userId);
        model.addAttribute("errorMessage", "수강 중인 과정이 없습니다.");

        return "participation/participationView";
      }

      LocalDate today = LocalDate.now();
      boolean isClassDay = attendanceService.isClassDay(today);

      model.addAttribute("isClassDay", isClassDay);
      model.addAttribute("today", today);
      model.addAttribute("learnerEnrollmentId", learnerEnrollmentId);
      model.addAttribute("loginUser", loginUser);

      if (isClassDay) {
        ParticipationVO todayParticipation = attendanceService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, today);
        model.addAttribute("todayParticipation", todayParticipation);

        // 과정 정보 조회 (수업 시간 등)
        CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(learnerEnrollmentId);
        if (course != null) {
          model.addAttribute("lessonEndTime", course.getLessonEndTime().toString());
          model.addAttribute("lessonEndPlus10", course.getLessonEndTime().plusMinutes(10).toString());
        }
      }

      // 진행률 계산
      double progressPercentage = attendanceService.getProgressPercentage(learnerEnrollmentId);
      model.addAttribute("progressPercentage", progressPercentage);

      return "participation/participationView";

    } catch (Exception e) {
      log.error("출결 조회 페이지 로드 중 오류 발생", e);
      model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
      return "participation/participationView";
    }
  }

  /**
   * 입실 처리 API
   */
  @PostMapping("/checkin")
  @ResponseBody
  public ResponseEntity<?> checkIn(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate participationDate = LocalDate.parse((String) request.get("participationDate"));
      LocalDateTime checkInTime = LocalDateTime.now();

      boolean result = attendanceService.processCheckIn(learnerEnrollmentId, checkInTime, participationDate);
      if (result) {
        ParticipationVO updated = attendanceService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, participationDate);
        return ResponseEntity.ok(
            Map.of("success", true, "message", "입실 처리 완료", "participation", updated));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "입실 처리 실패"));
      }
    } catch (Exception e) {
      log.error("입실 처리 중 오류 발생", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "입실 처리 중 오류"));
    }
  }

  /**
   * 퇴실 예상 상태 예측 API
   */
  @PostMapping("/predict-status")
  @ResponseBody
  public ResponseEntity<?> predictStatus(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate participationDate = LocalDate.parse((String) request.get("participationDate"));
      LocalDateTime predictedCheckOut = request.containsKey("predictedCheckOut")
          ? LocalDateTime.parse((String) request.get("predictedCheckOut"))
          : LocalDateTime.now();

      String predictedStatus = attendanceService.predictAttendanceStatus(
          learnerEnrollmentId, predictedCheckOut, participationDate);

      return ResponseEntity.ok(Map.of("success", true, "predictedStatus", predictedStatus));
    } catch (Exception e) {
      log.error("퇴실 상태 예측 중 오류 발생", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "상태 예측 실패"));
    }
  }

  /**
   * 퇴실 처리 API
   */
  @PostMapping("/checkout")
  @ResponseBody
  public ResponseEntity<?> checkOut(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate participationDate = LocalDate.parse((String) request.get("participationDate"));
      LocalDateTime checkOutTime = LocalDateTime.now();

      boolean result = attendanceService.processCheckOut(learnerEnrollmentId, checkOutTime, participationDate);
      if (result) {
        ParticipationVO updated = attendanceService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, participationDate);
        return ResponseEntity.ok(
            Map.of("success", true, "message", "퇴실 처리 완료", "participation", updated));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "퇴실 처리 실패"));
      }
    } catch (Exception e) {
      log.error("퇴실 처리 중 오류 발생", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "퇴실 처리 중 오류"));
    }
  }

  /**
   * 관리자용 - 날짜별 전체 출결 현황 조회 API
   */
  @GetMapping("/admin/date/{date}")
  @ResponseBody
  public ResponseEntity<?> getAttendanceByDateForAdmin(@PathVariable String date) {
    try {
      LocalDate participationDate = LocalDate.parse(date);
      List<ParticipationVO> participations = attendanceService.getParticipationByDateWithDisplayStatus(participationDate);

      return ResponseEntity.ok(Map.of("success", true, "date", date, "participations", participations));
    } catch (Exception e) {
      log.error("날짜별 출결 조회 중 오류 발생: ", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "출결 조회에 실패했습니다."));
    }
  }

  /**
   * 교육생 수업 진행률 조회 API
   */
  @GetMapping("/progress/{learnerEnrollmentId}")
  @ResponseBody
  public ResponseEntity<?> getProgressPercentage(@PathVariable Integer learnerEnrollmentId) {
    try {
      double progressPercentage = attendanceService.getProgressPercentage(learnerEnrollmentId);
      return ResponseEntity.ok(Map.of("success", true, "progressPercentage", progressPercentage));
    } catch (Exception e) {
      log.error("진행률 조회 중 오류 발생: ", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "진행률 조회에 실패했습니다."));
    }
  }

  /**
   * 과정 정보 조회 API (CourseController에서 이동)
   */
  @GetMapping("/course/{courseId}")
  @ResponseBody
  public ResponseEntity<?> getCourseInfo(@PathVariable Integer courseId) {
    try {
      CourseVO course = participationCourseMapper.selectCourseById(courseId);
      if (course != null) {
        return ResponseEntity.ok(Map.of("success", true, "course", course));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "존재하지 않는 과정입니다."));
      }
    } catch (Exception e) {
      log.error("과정 조회 중 오류 발생", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "과정 조회에 실패했습니다."));
    }
  }
}
