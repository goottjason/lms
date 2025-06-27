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
   * @param checkIn 입실 시간
   * @param checkOut 퇴실 시간
   * @param lunchStartTime 점심시작시간 (course.lunch_start_time)
   * @param lunchEndTime 점심종료시간 (course.lunch_end_time)
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
   * 출결 상태 판정 (과정별 daily_hours 반영)
   *
   * @param actualHours 실제 수업 시간(시간 단위)
   * @param isLate 지각 여부
   * @param dailyHours 해당 과정의 일일 훈련시간 (course.daily_hours)
   * @return 출결 상태 (ATTENDANCE, LATE, LEAVE_EARLY, ABSENCE)
   */
  public static String determineAttendanceStatus(long actualHours, boolean isLate, Integer dailyHours) {
    // dailyHours가 null이거나 0인 경우 기본값 8시간 사용
    int courseHours = (dailyHours != null && dailyHours > 0) ? dailyHours : 8;
    int halfCourseHours = courseHours / 2;

    if (actualHours >= courseHours) {
      return "ATTENDANCE"; // ✅ 과정 일일시간 이상: 출석
    } else if (actualHours >= halfCourseHours) {
      return isLate ? "LATE" : "LEAVE_EARLY"; // ✅ 과정 일일시간의 절반 이상: 지각/조퇴
    } else {
      return "ABSENCE"; // ✅ 과정 일일시간의 절반 미만: 결석
    }
  }

  /**
   * ✅ 기존 메서드 유지 (하위 호환성)
   * @deprecated 새로운 determineAttendanceStatus(long, boolean, Integer) 사용 권장
   */
  @Deprecated
  public static String determineAttendanceStatus(long actualHours, boolean isLate) {
    return determineAttendanceStatus(actualHours, isLate, 8); // 기본값 8시간으로 호출
  }

  /**
   * 상태별 인정 시간 계산 (과정별 daily_hours 반영)
   *
   * @param status 출결 상태
   * @param dailyHours 해당 과정의 일일 훈련시간 (course.daily_hours)
   * @return 인정 시간(시간 단위)
   */
  public static int calculateTrainingTime(String status, Integer dailyHours) {
    // dailyHours가 null이거나 0인 경우 기본값 8시간 사용
    int courseHours = (dailyHours != null && dailyHours > 0) ? dailyHours : 8;

    return switch (status) {
      case "ATTENDANCE", "VACATION" -> courseHours; // ✅ 과정별 일일훈련시간
      case "LATE", "LEAVE_EARLY" -> courseHours / 2; // ✅ 과정별 일일훈련시간의 1/2
      default -> 0;
    };
  }

  /**
   * ✅ 기존 메서드 유지 (하위 호환성)
   * @deprecated 새로운 calculateTrainingTime(String, Integer) 사용 권장
   */
  @Deprecated
  public static int calculateTrainingTime(String status) {
    return calculateTrainingTime(status, 8); // 기본값 8시간으로 호출
  }
}
