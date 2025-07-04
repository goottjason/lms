package com.goott5.lms.report.controller;

import com.goott5.lms.courseboarddebate.service.CourseBoardDebateService;
import com.goott5.lms.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController // 데이터 처리를 위한 @RestController 사용
@RequestMapping("/api/admin") // JavaScript의 요청 경로와 일치
@RequiredArgsConstructor
public class ReportApiController {

  private final ReportService reportService;
  private final CourseBoardDebateService courseBoardDebateService;

  /**
   * 신고 처리 (승인/거부)
   * @param reportId 경로 변수로 받은 신고 ID
   * @param payload 요청 본문(body)에 담긴 새로운 상태 값 { "status": "RESOLVED" }
   */
  @PutMapping("/reports/{reportId}")
  public ResponseEntity<Void> processReport(@PathVariable Long reportId, @RequestBody Map<String, String> payload) {
    log.info(">>>>>> 신고 처리 요청 수신: reportId={}, payload={} <<<<<<", reportId, payload);
    String newStatus = payload.get("status");

    // 유효하지 않은 status 값이 들어오면 에러 반환
    if (newStatus == null || (!newStatus.equals("RESOLVED") && !newStatus.equals("DISMISSED") && !newStatus.equals("DELETED"))) {
      return ResponseEntity.badRequest().build();
    }

    reportService.updateReportStatus(reportId, newStatus);

    return ResponseEntity.ok().build(); // 성공 시 200 OK 응답
  }

  /**
   * 게시글 즉시 삭제 (Soft Delete)
   * @param forumId 경로 변수로 받은 토론 게시글 ID
   */
  @DeleteMapping("/forums/{forumId}")
  public ResponseEntity<Void> deleteForumPost(@PathVariable int forumId) {
    log.info(">>>>>> 게시글 즉시 삭제 요청 수신: forumId={} <<<<<<", forumId);

    // 기존 토론 게시판 서비스의 삭제 메소드 호출
    courseBoardDebateService.deleteCourseBoardDebate(forumId);

    return ResponseEntity.ok().build(); // 성공 시 200 OK 응답
  }

  @GetMapping("/forums/getUser/{forumId}")
  public int getUserId(@PathVariable int forumId) {
    return courseBoardDebateService.getUserId(forumId);
  }
}