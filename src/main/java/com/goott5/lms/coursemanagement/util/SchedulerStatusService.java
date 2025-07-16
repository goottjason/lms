package com.goott5.lms.coursemanagement.util;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchedulerStatusService {

  private LocalDateTime lastExecutionTime;
  private LocalDateTime lastExecutionTimeForPart;

  // 수동으로 작동되면, 최근실행시간을 지금 시간으로 업데이트
  public void updateLastExecution() {
    this.lastExecutionTime = LocalDateTime.now();
    log.info("과정종료 스케줄러 최근작동시간: {}", this.lastExecutionTime);
  }
  public void updateLastExecutionForPart() {
    this.lastExecutionTimeForPart = LocalDateTime.now();
    log.info("출결 스케줄러 최근작동시간: {}", this.lastExecutionTimeForPart);
  }


  // 최근실행시간 가져오기
  public LocalDateTime getLastExecutionTime() {
    return lastExecutionTime;
  }
  public LocalDateTime getLastExecutionTimeForPart() {return lastExecutionTimeForPart; }
}