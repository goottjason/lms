package com.goott5.lms.participation.controller;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.service.ParticipationService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import java.util.stream.Collectors;
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
 * 교육생 출결(participation) 관련 웹 컨트롤러
 */
@Controller
@Slf4j
@RequestMapping("/participation")
@RequiredArgsConstructor
public class ParticipationController {

  private final ParticipationService participationService;
  private final ParticipationCourseMapper participationCourseMapper;

  // ========== 페이지 렌더링 ==========

  /**
   * 출결 조회 메인 페이지
   */
  @GetMapping("/participationView")
  public String participationView(Model model, HttpSession session) {
    try {
      // 세션에서 로그인 사용자 정보 가져오기
      UserVO loginUser = (UserVO) session.getAttribute("loginUser");

      Integer userId = loginUser.getId();
      log.info("출결 조회 페이지 접근 - 사용자 ID: {}", userId);

      // 사용자의 현재 수강 정보 조회
      Integer learnerEnrollmentId = participationCourseMapper.selectLearnerEnrollmentIdByUserId(userId);
      if (learnerEnrollmentId == null) {
        log.warn("수강 중인 과정이 없습니다 - userId: {}", userId);
        model.addAttribute("errorMessage", "수강 중인 과정이 없습니다.");
        model.addAttribute("isClassDay", false);
        return "participation/participationView";
      }

      // 현재 과정 정보 조회
      CourseVO currentCourse = participationCourseMapper.selectCourseByLearnerEnrollmentId(learnerEnrollmentId);

      // 오늘 날짜 및 수업일 여부 확인
      LocalDate today = LocalDate.now();
      boolean isClassDay = participationService.isClassDay(today);

      // 모델에 기본 정보 추가
      model.addAttribute("loginUser", loginUser);
      model.addAttribute("currentCourse", currentCourse);
      model.addAttribute("learnerEnrollmentId", learnerEnrollmentId);
      model.addAttribute("today", today);
      model.addAttribute("isClassDay", isClassDay);

      // 수업일인 경우 오늘 출결 정보 조회
      if (isClassDay) {
        ParticipationVO todayParticipation = participationService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, today);
        model.addAttribute("todayParticipation", todayParticipation);
      }

      // ✅ 수정: 어제까지의 출결 통계 데이터 (오늘 기본 결석 제외)
      Map<String, Object> attendanceStats = participationService.getAttendanceStatsUntilYesterday(learnerEnrollmentId);
      model.addAttribute("attendanceStats", attendanceStats);

      // ✅ 수정: 어제까지의 과정 진행률 (실제 진행일수 기준)
      Map<String, Object> courseProgress = participationService.getCourseProgressUntilYesterday(learnerEnrollmentId);
      model.addAttribute("courseProgress", courseProgress);

      return "participation/participationView";
    } catch (Exception e) {
      log.error("출결 조회 페이지 로드 중 오류 발생", e);
      model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
      model.addAttribute("isClassDay", false);
      return "participation/participationView";
    }
  }



  // ========== 조회 API ==========

  /**
   * 월별 출결 데이터 조회 (캘린더용)
   */
  @GetMapping("/monthly/{learnerEnrollmentId}/{date}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getMonthlyParticipation(
      @PathVariable Integer learnerEnrollmentId,
      @PathVariable String date,
      HttpServletResponse response) {
    try {
      // 캐시 방지 헤더 설정
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");

      LocalDate startDate = LocalDate.parse(date);
      LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

      //  최신 DB 상태를 반영한 출결 데이터 조회
      List<ParticipationVO> participations = participationService.getParticipationByLearnerEnrollmentIdAndDateRange(
          learnerEnrollmentId, startDate, endDate);

      log.debug("월별 출결 조회 성공 - learnerEnrollmentId: {}, 조회기간: {} ~ {}, 건수: {}",
          learnerEnrollmentId, startDate, endDate, participations.size());

      return ResponseEntity.ok(Map.of(
          "success", true,
          "participations", participations
      ));
    } catch (Exception e) {
      log.error("월별 출결 조회 오류 - learnerEnrollmentId: {}, date: {}", learnerEnrollmentId, date, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "월별 출결 조회에 실패했습니다."
      ));
    }
  }

  /**
   * 사용자의 현재/이전 수강 과정 목록 조회
   */
  @GetMapping("/courses/{userId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getUserCourses(@PathVariable Integer userId) {
    try {
      CourseVO currentCourse = participationService.getCurrentCourseByUserId(userId);
      List<CourseVO> previousCourses = participationService.getPreviousCoursesByUserId(userId);

      log.debug("과정 목록 조회 성공 - userId: {}, 현재과정: {}, 이전과정수: {}",
          userId, currentCourse != null ? currentCourse.getName() : "없음", previousCourses.size());

      return ResponseEntity.ok(Map.of(
          "success", true,
          "currentCourse", currentCourse,
          "previousCourses", previousCourses
      ));

    } catch (Exception e) {
      log.error("과정 목록 조회 중 오류 발생 - userId: {}", userId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "과정 목록 조회에 실패했습니다."
      ));
    }
  }

  /**
   * 특정 learnerEnrollmentId의 통계 조회
   */
  @GetMapping("/stats/{learnerEnrollmentId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getStats(@PathVariable Integer learnerEnrollmentId) {
    try {
      log.debug("통계 조회 요청 - learnerEnrollmentId: {}", learnerEnrollmentId);

      // 어제까지의 출결 통계와 과정 진행률 조회
      Map<String, Object> attendanceStats = participationService.getAttendanceStatsUntilYesterday(learnerEnrollmentId);
      Map<String, Object> courseProgress = participationService.getCourseProgressUntilYesterday(learnerEnrollmentId);

      // 합쳐서 반환
      Map<String, Object> result = new HashMap<>(attendanceStats);
      result.putAll(courseProgress);
      result.put("success", true);

      log.debug("통계 조회 성공 - learnerEnrollmentId: {}", learnerEnrollmentId);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("통계 조회 오류 - learnerEnrollmentId: {}", learnerEnrollmentId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "통계 조회에 실패했습니다."
      ));
    }
  }

  /**
   * 특정 사용자와 과정 ID로 learnerEnrollmentId 조회
   */
  @GetMapping("/learner-enrollment/{userId}/{courseId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getLearnerEnrollmentId(
      @PathVariable Integer userId,
      @PathVariable Integer courseId) {
    try {
      log.debug("learnerEnrollmentId 조회 요청 - userId: {}, courseId: {}", userId, courseId);

      Integer learnerEnrollmentId = participationService.getLearnerEnrollmentIdByUserIdAndCourseId(userId, courseId);

      if (learnerEnrollmentId != null) {
        log.debug("learnerEnrollmentId 조회 성공: {}", learnerEnrollmentId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "learnerEnrollmentId", learnerEnrollmentId
        ));
      } else {
        log.warn("learnerEnrollmentId 조회 실패 - userId: {}, courseId: {}", userId, courseId);
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "해당 과정의 수강 정보를 찾을 수 없습니다."
        ));
      }
    } catch (Exception e) {
      log.error("learnerEnrollmentId 조회 오류 - userId: {}, courseId: {}", userId, courseId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "수강 정보 조회에 실패했습니다."
      ));
    }
  }

  /**
   * 교육생 출석률 조회
   */
  @GetMapping("/attendance-rate/{learnerEnrollmentId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getAttendanceRate(@PathVariable Integer learnerEnrollmentId) {
    try {
      double attendanceRate = participationService.getAttendanceRate(learnerEnrollmentId);

      log.debug("출석률 조회 성공 - learnerEnrollmentId: {}, 출석률: {}%", learnerEnrollmentId, attendanceRate);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "attendanceRate", attendanceRate
      ));

    } catch (Exception e) {
      log.error("출석률 조회 중 오류 발생 - learnerEnrollmentId: {}", learnerEnrollmentId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "출석률 조회에 실패했습니다."
      ));
    }
  }

  /**
   * 과정 정보 조회
   */
  @GetMapping("/course/{courseId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getCourseInfo(@PathVariable Integer courseId) {
    try {
      CourseVO course = participationCourseMapper.selectCourseById(courseId);

      if (course != null) {
        log.debug("과정 조회 성공 - courseId: {}, 과정명: {}", courseId, course.getName());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "course", course
        ));
      } else {
        log.warn("존재하지 않는 과정 - courseId: {}", courseId);
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "존재하지 않는 과정입니다."
        ));
      }

    } catch (Exception e) {
      log.error("과정 조회 중 오류 발생 - courseId: {}", courseId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "과정 조회에 실패했습니다."
      ));
    }
  }

  // ========== 출결 처리 API ==========

  /**
   * 입실 처리
   */
  @PostMapping("/checkin")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate participationDate = LocalDate.parse((String) request.get("participationDate"));
      LocalDateTime checkInTime = LocalDateTime.now();

      log.info("입실 처리 시작 - learnerEnrollmentId: {}, participationDate: {}",
          learnerEnrollmentId, participationDate);

      boolean result = participationService.processCheckIn(learnerEnrollmentId, checkInTime, participationDate);

      if (result) {
        ParticipationVO updated = participationService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, participationDate);

        log.info("입실 처리 성공 - learnerEnrollmentId: {}", learnerEnrollmentId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "입실 처리가 완료되었습니다.",
            "participation", updated
        ));
      } else {
        log.warn("입실 처리 실패 - learnerEnrollmentId: {}", learnerEnrollmentId);
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "입실 처리에 실패했습니다."
        ));
      }

    } catch (Exception e) {
      log.error("입실 처리 중 오류 발생", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "입실 처리 중 오류가 발생했습니다."
      ));
    }
  }

  /**
   * 퇴실 예상 상태 예측
   */
  @PostMapping("/predict-status")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> predictStatus(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate participationDate = LocalDate.parse((String) request.get("participationDate"));

      // ✅ 수정: 다양한 시간 형식 처리
      LocalDateTime predictedCheckOut;
      if (request.containsKey("predictedCheckOut")) {
        String predictedCheckOutStr = (String) request.get("predictedCheckOut");
        try {
          // UTC 타임존이 포함된 경우 (예: 2025-06-23T06:44:27.135Z)
          if (predictedCheckOutStr.endsWith("Z") || predictedCheckOutStr.contains("+")) {
            predictedCheckOut = OffsetDateTime.parse(predictedCheckOutStr).toLocalDateTime();
          } else {
            // 일반적인 LocalDateTime 형식
            predictedCheckOut = LocalDateTime.parse(predictedCheckOutStr);
          }
        } catch (Exception parseException) {
          log.warn("시간 파싱 실패, 현재 시간으로 대체: {}", predictedCheckOutStr);
          predictedCheckOut = LocalDateTime.now();
        }
      } else {
        predictedCheckOut = LocalDateTime.now();
      }

      String predictedStatus = participationService.predictAttendanceStatus(
          learnerEnrollmentId, predictedCheckOut, participationDate);

      return ResponseEntity.ok(Map.of("success", true, "predictedStatus", predictedStatus));
    } catch (Exception e) {
      log.error("퇴실 상태 예측 중 오류 발생", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "상태 예측 실패"));
    }
  }


  /**
   * 퇴실 처리
   */
  @PostMapping("/checkout")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> checkOut(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate participationDate = LocalDate.parse((String) request.get("participationDate"));
      LocalDateTime checkOutTime = LocalDateTime.now();

      log.info("퇴실 처리 시작 - learnerEnrollmentId: {}, participationDate: {}",
          learnerEnrollmentId, participationDate);

      boolean result = participationService.processCheckOut(learnerEnrollmentId, checkOutTime, participationDate);

      if (result) {
        ParticipationVO updated = participationService.getTodayParticipationWithDisplayStatus(
            learnerEnrollmentId, participationDate);

        log.info("퇴실 처리 성공 - learnerEnrollmentId: {}", learnerEnrollmentId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "퇴실 처리가 완료되었습니다.",
            "participation", updated
        ));
      } else {
        log.warn("퇴실 처리 실패 - learnerEnrollmentId: {}", learnerEnrollmentId);
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "퇴실 처리에 실패했습니다."
        ));
      }

    } catch (Exception e) {
      log.error("퇴실 처리 중 오류 발생", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "퇴실 처리 중 오류가 발생했습니다."
      ));
    }
  }

  /**
   * 휴가 신청 가능한 날짜 목록 조회
   */
  @GetMapping("/available-vacation-dates/{learnerEnrollmentId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getAvailableVacationDates(
      @PathVariable Integer learnerEnrollmentId) {
    try {
      log.debug("휴가 신청 가능 날짜 조회 - learnerEnrollmentId: {}", learnerEnrollmentId);

      List<LocalDate> availableDates = participationService.getAvailableVacationDates(learnerEnrollmentId);

      // 문자열 형태로 변환 (JavaScript에서 사용하기 위해)
      List<String> availableDateStrings = availableDates.stream()
          .map(LocalDate::toString)
          .collect(Collectors.toList());

      log.debug("휴가 신청 가능 날짜 개수: {}", availableDates.size());

      return ResponseEntity.ok(Map.of(
          "success", true,
          "availableDates", availableDateStrings
      ));
    } catch (Exception e) {
      log.error("휴가 신청 가능 날짜 조회 오류 - learnerEnrollmentId: {}", learnerEnrollmentId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "휴가 신청 가능 날짜 조회에 실패했습니다."
      ));
    }
  }




}
