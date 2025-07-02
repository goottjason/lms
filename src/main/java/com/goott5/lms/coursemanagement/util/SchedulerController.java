package com.goott5.lms.coursemanagement.util;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SchedulerController {

  private final CourseEndScheduler courseEndScheduler;
  private final SchedulerStatusService schedulerStatusService;
  // private final CourseSchedulerService schedulerService;
  // private final SchedulerStatusService statusService;

  @PostMapping("/api/scheduler/trigger-end-course-process")
  public ResponseEntity<String> triggerEndCourseProcess() {
    try {
      // 기존 스케줄러 로직 호출
      courseEndScheduler.endCourseAutoProcess();
      // 수동으로 작동되면, 최근실행시간을 지금 시간으로 업데이트
      schedulerStatusService.updateLastExecution();
      return ResponseEntity.ok("Scheduler executed successfully");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Execution failed: " + e.getMessage());
    }
  }

  @GetMapping("/api/scheduler/last-execution")
  public ResponseEntity<LocalDateTime> getLastExecutionTime() {
    return ResponseEntity.ok(schedulerStatusService.getLastExecutionTime());
  }
}