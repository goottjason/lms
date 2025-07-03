package com.goott5.lms.coursemanagement.util;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class SchedulerStatusService {

  private LocalDateTime lastExecutionTime;
  private LocalDateTime lastExecutionTimeForPart;

  // 수동으로 작동되면, 최근실행시간을 지금 시간으로 업데이트
  public void updateLastExecution() {
    this.lastExecutionTime = LocalDateTime.now();
  }
  public void updateLastExecutionForPart() { this.lastExecutionTimeForPart = LocalDateTime.now(); }


  // 최근실행시간 가져오기
  public LocalDateTime getLastExecutionTime() {
    return lastExecutionTime;
  }
  public LocalDateTime getLastExecutionTimeForPart() {return lastExecutionTimeForPart; }
}