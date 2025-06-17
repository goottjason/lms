package com.goott5.lms.participation.controller;

import com.goott5.lms.participation.service.VacationService;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 휴가(Vacation) 관련 웹 컨트롤러
 */
@Controller
@RequestMapping("/vacation")
@RequiredArgsConstructor
public class VacationController {

  private final VacationService vacationService;

  /**
   * 휴가 신청 API
   */
  @PostMapping("/apply")
  @ResponseBody
  public ResponseEntity<?> applyVacation(@RequestBody Map<String, Object> request) {
    try {
      Integer learnerEnrollmentId = (Integer) request.get("learnerEnrollmentId");
      LocalDate vacationDate = LocalDate.parse((String) request.get("vacationDate"));
      String explanation = (String) request.get("explanation");
      boolean result = vacationService.applyVacation(learnerEnrollmentId, vacationDate,
          explanation);
      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 신청 완료, 강사 승인 대기"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 신청 실패"));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 신청 중 오류"));
    }
  }

  /**
   * 휴가 승인 API (강사용)
   */
  @PostMapping("/approve/{participationId}")
  @ResponseBody
  public ResponseEntity<?> approveVacation(@PathVariable Integer participationId) {
    try {
      boolean result = vacationService.approveVacation(participationId);
      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 승인 완료"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 승인 실패"));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 승인 중 오류"));
    }
  }

  /**
   * 휴가 거부 API (강사용)
   */
  @PostMapping("/reject/{participationId}")
  @ResponseBody
  public ResponseEntity<?> rejectVacation(@PathVariable Integer participationId) {
    try {
      boolean result = vacationService.rejectVacation(participationId);
      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 거부 완료"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 거부 실패"));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 거부 중 오류"));
    }
  }

  /**
   * 휴가 삭제 API (교육생/강사용)
   */
  @DeleteMapping("/{participationId}")
  @ResponseBody
  public ResponseEntity<?> deleteVacation(@PathVariable Integer participationId) {
    try {
      boolean result = vacationService.deleteVacation(participationId);
      if (result) {
        return ResponseEntity.ok(Map.of("success", true, "message", "휴가 삭제 완료"));
      } else {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 삭제 실패"));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "휴가 삭제 중 오류"));
    }
  }
}
