package com.goott5.lms.participation.util;

import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.service.ParticipationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 출결 스케줄러 (course_schedule 기반)
 * 매일 자정에 다음 작업을 수행:
 * 1. 전날까지 미완료된 출결 기록들을 결석 처리
 * 2. 오늘 수업이 있는 과정의 모든 수강생에게 기본 출결 기록 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

  private final ParticipationService participationService;
  private final ParticipationCourseMapper participationCourseMapper;

  /**
   * 매일 오전 00시 00분 (자정) 에 출결 관련 작업 수행
   * 1. 전날까지 미완료 기록 결석 처리 (입실했지만 퇴실 안한 기록)
   * 2. 오늘 수업이 있는 과정의 기본 출결 기록 생성
   *
   * course_schedule 테이블 기반으로 수업이 있는 날에만 실행
   * 주말/공휴일/휴강일은 course_schedule에 데이터가 없으므로 아무것도 하지 않음
   */
  @Scheduled(cron = "0 15 15 * * MON-FRI", zone = "Asia/Seoul")
  public void createDailyAttendanceRecords() {
    LocalDate today = LocalDate.now();
    log.info("===== 출결 스케줄러 실행 시작: {} =====", today);

    try {
      // ✅ 1단계: 전날까지 미완료 출결 기록들을 결석 처리
      log.info("1단계: 미완료 출결 기록 결석 처리 시작");
      participationService.processIncompleteRecordsFromPreviousDays(today);
      log.info("1단계 완료: 미완료 출결 기록 결석 처리 완료");

      // ✅ 2단계: 오늘 수업이 있는 과정들 조회
      log.info("2단계: 오늘 수업 과정 조회 및 출결 기록 생성 시작");
      List<Integer> courseIds = participationCourseMapper.selectCoursesBySchedule(today);
      log.info("오늘 수업이 있는 과정 수: {}", courseIds.size());

      if (courseIds.isEmpty()) {
        log.info("오늘은 수업이 없는 날입니다 (주말/공휴일/휴강일): {}", today);
        return;
      }

      // ✅ 3단계: 각 과정의 모든 수강생에게 출결 기록 생성
      int totalCreated = 0;
      for (Integer courseId : courseIds) {
        int created = participationService.createDailyAttendanceForCourse(courseId, today);
        totalCreated += created;
        log.info("과정 {} 출결 기록 생성 완료: {}건", courseId, created);
      }

      log.info("전체 출결 기록 생성 완료: {}개 과정, {}건의 출결 기록", courseIds.size(), totalCreated);

    } catch (Exception e) {
      log.error("출결 스케줄러 실행 중 오류 발생: ", e);
    }

    log.info("===== 출결 스케줄러 실행 종료: {} =====", today);
  }

  /**
   * 수동 테스트용 메서드 (필요시 사용)
   * 특정 날짜의 미완료 기록들을 수동으로 처리할 때 사용
   */
  public void manualProcessIncompleteRecords(LocalDate targetDate) {
    log.info("수동 미완료 기록 처리 시작: {}", targetDate);
    try {
      participationService.processIncompleteRecordsFromPreviousDays(targetDate);
      log.info("수동 미완료 기록 처리 완료: {}", targetDate);
    } catch (Exception e) {
      log.error("수동 미완료 기록 처리 중 오류: {}", targetDate, e);
      throw e;
    }
  }

}
