package com.goott5.lms.participation.controller;

import com.goott5.lms.participation.service.VacationService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 휴가(Vacation) 관련 웹 컨트롤러
 */
@Controller
@RequestMapping("/vacation")
@RequiredArgsConstructor
@Slf4j
public class VacationController {

  private final VacationService vacationService;




  // ========== REST API ==========

  /**
   * 휴가 신청 API
   */
  @PostMapping("/apply")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> applyVacation(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate vacationDate = LocalDate.parse((String) request.get("vacationDate"));
      String explanation = (String) request.get("explanation");

      boolean result = vacationService.applyVacation(learnerEnrollmentId, vacationDate, explanation);

      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 신청 완료, 강사 승인 대기"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 신청 실패"));
      }
    } catch (Exception e) {
      log.error("휴가 신청 중 오류", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 신청 중 오류"));
    }
  }

  /**
   * 휴가 승인 API (강사용)
   */
  @PostMapping("/approve/{participationId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> approveVacation(@PathVariable Integer participationId) {
    try {
      boolean result = vacationService.approveVacation(participationId);

      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 승인 완료"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 승인 실패"));
      }
    } catch (Exception e) {
      log.error("휴가 승인 중 오류", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 승인 중 오류"));
    }
  }

  /**
   * 휴가 거부 API (강사용)
   */
  @PostMapping("/reject/{participationId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> rejectVacation(@PathVariable Integer participationId) {
    try {
      boolean result = vacationService.rejectVacation(participationId);

      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 거부 완료"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 거부 실패"));
      }
    } catch (Exception e) {
      log.error("휴가 거부 중 오류", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 거부 중 오류"));
    }
  }

  /**
   * 휴가 삭제 API (교육생/강사용)
   */
  @DeleteMapping("/{participationId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> deleteVacation(@PathVariable Integer participationId) {
    try {
      boolean result = vacationService.deleteVacation(participationId);

      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 삭제 완료"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 삭제 실패"));
      }
    } catch (Exception e) {
      log.error("휴가 삭제 중 오류", e);
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 삭제 중 오류"));
    }
  }

  /**
   * 휴가 데이터 조회 API
   */
  @GetMapping("/api/vacations/{courseId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getVacationData(
      @PathVariable Integer courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "all") String type) {

    try {
      Page<Map<String, Object>> vacationData;

      if ("approved".equals(type)) {
        // 과거 과정: 승인된 휴가만
        vacationData = vacationService.getApprovedVacationsByCourse(courseId, page, size);
      } else {
        // 현재 과정: 승인 대기 + 승인된 휴가 모두
        vacationData = vacationService.getAllVacationsByCourse(courseId, page, size);
      }

      return ResponseEntity.ok(Map.of(
          "success", true,
          "data", vacationData
      ));

    } catch (Exception e) {
      log.error("휴가 데이터 조회 중 오류 - courseId: {}, type: {}", courseId, type, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "휴가 데이터 조회에 실패했습니다."
      ));
    }
  }

  /**
   * 휴가 신청 목록 검색 API (이름으로 검색)
   */
  @GetMapping("/api/vacations/{courseId}/search")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> searchVacationData(
      @PathVariable Integer courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "all") String type,
      @RequestParam(required = false) String searchName) {

    try {
      Page<Map<String, Object>> vacationData;

      if ("approved".equals(type)) {
        // 과거 과정: 승인된 휴가만 (검색 포함)
        vacationData = vacationService.searchApprovedVacationsByCourse(courseId, page, size, searchName);
      } else {
        // 현재 과정: 승인 대기 + 승인된 휴가 모두 (검색 포함)
        vacationData = vacationService.searchAllVacationsByCourse(courseId, page, size, searchName);
      }

      return ResponseEntity.ok(Map.of(
          "success", true,
          "data", vacationData
      ));

    } catch (Exception e) {
      log.error("휴가 데이터 검색 중 오류 - courseId: {}, searchName: {}", courseId, searchName, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "휴가 데이터 검색에 실패했습니다."
      ));
    }
  }



}
