package com.goott5.lms.participation.util;

import com.goott5.lms.participation.service.AttendanceService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

  private final AttendanceService attendanceService;

  /**
   * 매일 자정 00:00:00에 출결 기록 생성 (평일) isClassDayForCourse 메서드 활용
   */
  @Scheduled(cron = "0 0 0 * * MON-FRI", zone = "Asia/Seoul")
  public void createDailyAttendanceRecords() {
    LocalDate today = LocalDate.now();
    log.info("===== 출결 스케줄러 실행 시작: {} =====", today);

    try {
      // 전체적인 수업일 여부 확인
      if (attendanceService.isClassDay(today)) {
        attendanceService.createDailyAttendanceForAllCourses(today);
        log.info("출결 스케줄러 실행 완료: {}", today);
      } else {
        log.info("오늘은 전체 휴강일입니다: {}", today);
      }

    } catch (Exception e) {
      log.error("출결 스케줄러 실행 중 오류 발생: ", e);
    }

    log.info("===== 출결 스케줄러 실행 종료: {} =====", today);
  }
}
