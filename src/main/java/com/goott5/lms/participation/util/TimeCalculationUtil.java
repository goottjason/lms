package com.goott5.lms.participation.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;

/**
 * 출결 시간 계산 유틸리티 점심시간 제외, 인정시간/상태 판정 등
 */
@Slf4j
public class TimeCalculationUtil {

  /**
   * 실제 수업 시간(분) 계산 (점심시간 제외)
   *
   * @param checkIn        입실 시간
   * @param checkOut       퇴실 시간
   * @param lunchStartTime 점심시작시간 (course.lunch_start_time)
   * @param lunchEndTime   점심종료시간 (course.lunch_end_time)
   * @return 실제 수업 시간(분)
   */
  public static long calculateActualStudyMinutes(LocalDateTime checkIn, LocalDateTime checkOut,
      LocalTime lunchStartTime, LocalTime lunchEndTime) {
    if (checkIn == null || checkOut == null) {
      return 0;
    }

    // 같은 날인지 확인 추가
    if (!checkIn.toLocalDate().equals(checkOut.toLocalDate())) {
      log.warn("입실과 퇴실이 다른 날짜: checkIn={}, checkOut={}", checkIn, checkOut);
      return 0;
    }

    long totalMinutes = Duration.between(checkIn, checkOut).toMinutes();
    LocalTime checkInTime = checkIn.toLocalTime();
    LocalTime checkOutTime = checkOut.toLocalTime();

    // 점심시간이 수업시간에 포함되는 경우 점심시간 제외
    if (checkInTime.isBefore(lunchEndTime) && checkOutTime.isAfter(lunchStartTime)) {
      long lunchMinutes = Duration.between(lunchStartTime, lunchEndTime).toMinutes();
      totalMinutes -= lunchMinutes;
    }

    return Math.max(0, totalMinutes);
  }

  /**
   * 출결 상태 판정 (출석, 결석, 지각, 조퇴)
   *
   * @param actualHours 실제 수업 시간(시간 단위)
   * @param isLate      지각 여부
   * @return 출결 상태 (ATTENDANCE, LATE, LEAVE_EARLY, ABSENCE)
   */
  public static String determineAttendanceStatus(long actualHours, boolean isLate) {
    if (actualHours >= 8) {
      return "ATTENDANCE";
    } else if (actualHours >= 4) {
      return isLate ? "LATE" : "LEAVE_EARLY";
    } else {
      return "ABSENCE";
    }
  }

  /**
   * 상태별 인정 시간 계산
   *
   * @param status 출결 상태
   * @return 인정 시간(시간 단위)
   */
  public static int calculateTrainingTime(String status) {
    return switch (status) {
      case "ATTENDANCE", "VACATION" -> 8;
      case "LATE", "LEAVE_EARLY" -> 4;
      default -> 0;
    };
  }
}
