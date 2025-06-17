package com.goott5.lms.participation.controller;


import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationCourseMapper;

import com.goott5.lms.participation.service.AttendanceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
   * 출결 조회 메인 페이지 (진행률 포함)
   */
  @GetMapping("/participationView")
  public String participationView(Model model) {

//    LocalDate today = LocalDate.now();
//    boolean isClassDay = attendanceService.isClassDay(today);
//    Integer learnerEnrollmentId = 1; // TODO: 로그인 세션에서 가져오기
//
//    model.addAttribute("isClassDay", isClassDay);
//    model.addAttribute("today", today);
//    model.addAttribute("learnerEnrollmentId", learnerEnrollmentId);
//
//    if (isClassDay) {
//      ParticipationVO todayParticipation = attendanceService.getTodayParticipationWithDisplayStatus(
//          learnerEnrollmentId, today);
//      model.addAttribute("todayParticipation", todayParticipation);
//    }
//
//    // 진행률 계산
//    double progressPercentage = attendanceService.getProgressPercentage(learnerEnrollmentId);
//    model.addAttribute("progressPercentage", progressPercentage);


    return "participation/participationView";
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
      boolean result = attendanceService.processCheckIn(learnerEnrollmentId, checkInTime,
          participationDate);
      if (result) {
        ParticipationVO updated = attendanceService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, participationDate);
        return ResponseEntity.ok(
            Map.of("success", true, "message", "입실 처리 완료", "participation", updated));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "입실 처리 실패"));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "입실 처리 중 오류"));
    }
  }

  /**
   * 퇴실 예상 상태 예측 (모달버튼) API
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
          learnerEnrollmentId, predictedCheckOut, participationDate
      );
      return ResponseEntity.ok(Map.of(
          "success", true,
          "predictedStatus", predictedStatus
      ));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "상태 예측 실패"
      ));
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
      boolean result = attendanceService.processCheckOut(learnerEnrollmentId, checkOutTime,
          participationDate);
      if (result) {
        ParticipationVO updated = attendanceService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, participationDate);
        return ResponseEntity.ok(
            Map.of("success", true, "message", "퇴실 처리 완료", "participation", updated));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "퇴실 처리 실패"));
      }
    } catch (Exception e) {
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
      List<ParticipationVO> participations = attendanceService.getParticipationByDateWithDisplayStatus(
          participationDate);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "date", date,
          "participations", participations
      ));

    } catch (Exception e) {
      log.error("날짜별 출결 조회 중 오류 발생: ", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "출결 조회에 실패했습니다."
      ));
    }
  }

  /**
   * 관리자용 - 캘린더 화면
   */
  @GetMapping("/admin/calendar")
  public String participationCalendar(Model model) {
    LocalDate today = LocalDate.now();

    // 이번 달 출결 현황
    List<ParticipationVO> monthlyParticipations = attendanceService.getParticipationByDateWithDisplayStatus(
        today);

    model.addAttribute("today", today);
    model.addAttribute("monthlyParticipations", monthlyParticipations);

    return "participation/calendar";  // 캘린더 화면
  }

  /**
   * 과정별 수업일 여부 확인 API
   */
  @GetMapping("/course/{courseId}/class-day/{date}")
  @ResponseBody
  public ResponseEntity<?> checkClassDay(@PathVariable Integer courseId,
      @PathVariable String date) {
    try {
      LocalDate checkDate = LocalDate.parse(date);
      boolean isClassDay = attendanceService.isClassDayForCourse(courseId, checkDate);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "courseId", courseId,
          "date", date,
          "isClassDay", isClassDay
      ));

    } catch (Exception e) {
      log.error("수업일 확인 중 오류 발생: ", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "수업일 확인에 실패했습니다."
      ));
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

      return ResponseEntity.ok(Map.of(
          "success", true,
          "progressPercentage", progressPercentage
      ));

    } catch (Exception e) {
      log.error("진행률 조회 중 오류 발생: ", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "진행률 조회에 실패했습니다."
      ));
    }
  }


  /**
   * 과정 정보 조회 API
   */
  @GetMapping("/course/{courseId}")
  @ResponseBody
  public ResponseEntity<?> getCourseInfo(@PathVariable Integer courseId) {
    try {
      CourseVO course = participationCourseMapper.selectCourseById(courseId);

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
