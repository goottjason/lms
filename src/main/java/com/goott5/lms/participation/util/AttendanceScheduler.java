package com.goott5.lms.participation.util;

import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.service.AttendanceService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 출결 스케줄러 (course_schedule 기반)
 * 매일 자정에 수업이 있는 과정의 모든 수강생에게 기본 출결 기록 생성
 * 이미 출결 기록이 있는 학생(휴가 승인받은 학생 등)은 건드리지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

  private final AttendanceService attendanceService;
  private final ParticipationCourseMapper participationCourseMapper;

  /**
   * 매일 자정 00:00:00에 출결 기록 생성
   * course_schedule 테이블 기반으로 수업이 있는 날에만 실행
   * 주말/공휴일/휴강일은 course_schedule에 데이터가 없으므로 아무것도 하지 않음
   */
  @Scheduled(cron = "0 0 0 * * MON-FRI", zone = "Asia/Seoul")
  public void createDailyAttendanceRecords() {
    LocalDate today = LocalDate.now();
    log.info("===== 출결 스케줄러 실행 시작: {} =====", today);

    try {
      // course_schedule에서 오늘 수업이 있는 과정들 조회
      List<Integer> courseIds = participationCourseMapper.selectCoursesBySchedule(today);
      log.info("오늘 수업이 있는 과정 수: {}", courseIds.size());

      if (courseIds.isEmpty()) {
        log.info("오늘은 수업이 없는 날입니다 (주말/공휴일/휴강일): {}", today);
        return;
      }

      // 각 과정의 모든 수강생에게 출결 기록 생성
      int totalCreated = 0;
      for (Integer courseId : courseIds) {
        int created = attendanceService.createDailyAttendanceForCourse(courseId, today);
        totalCreated += created;
        log.info("과정 {} 출결 기록 생성 완료: {}건", courseId, created);
      }

      log.info("전체 출결 기록 생성 완료: {}개 과정, {}건의 출결 기록", courseIds.size(), totalCreated);

    } catch (Exception e) {
      log.error("출결 스케줄러 실행 중 오류 발생: ", e);
    }

    log.info("===== 출결 스케줄러 실행 종료: {} =====", today);
  }
}
