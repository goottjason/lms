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
   * @param lunchStart 점심시작시간 (course.lunch_start_time)
   * @param lunchEnd 점심종료시간 (course.lunch_end_time)
   * @return 실제 수업 시간(분)
   */
  public static long calculateActualStudyMinutes(LocalDateTime checkIn, LocalDateTime checkOut,
                                                 LocalTime lunchStart, LocalTime lunchEnd) {
    if (checkIn == null || checkOut == null || lunchStart == null || lunchEnd == null) {
      return 0;
    }
    long totalMinutes = Duration.between(checkIn, checkOut).toMinutes();
    long lunchOverlapMinutes = calculateOverlapMinutes(
            checkIn.toLocalTime(), checkOut.toLocalTime(), lunchStart, lunchEnd
    );
    return totalMinutes - lunchOverlapMinutes;
  }

  private static long calculateOverlapMinutes(LocalTime studentStart, LocalTime studentEnd,
                                              LocalTime lunchStart, LocalTime lunchEnd) {
    LocalTime overlapStart = studentStart.isAfter(lunchStart) ? studentStart : lunchStart;
    LocalTime overlapEnd = studentEnd.isBefore(lunchEnd) ? studentEnd : lunchEnd;
    if (overlapStart.isBefore(overlapEnd)) {
      return Duration.between(overlapStart, overlapEnd).toMinutes();
    }
    return 0;
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
    // dailyHours가 null이거나 0 이하이면 오류를 발생시켜 잘못된 계산을 막습니다.
    if (dailyHours == null || dailyHours <= 0) {
      throw new IllegalArgumentException("유효하지 않은 소정훈련시간(dailyHours)이 제공되었습니다: " + dailyHours);
    }

    int requiredHours = dailyHours;
    int halfHours = requiredHours / 2;

    if (actualHours >= requiredHours) {
      return "ATTENDANCE";
    } else if (actualHours >= halfHours) {
      return isLate ? "LATE" : "LEAVE_EARLY";
    } else {
      return "ABSENCE";
    }
  }


  /**
   * 상태별 인정 시간 계산 (과정별 daily_hours 반영)
   *
   * @param status 출결 상태
   * @param dailyHours 해당 과정의 일일 훈련시간 (course.daily_hours)
   * @return 인정 시간(시간 단위)
   */
  public static int calculateTrainingTime(String status, Integer dailyHours) {
    // dailyHours가 null이거나 0 이하이면 오류를 발생시켜 잘못된 계산을 차단
    if (dailyHours == null || dailyHours <= 0) {
      throw new IllegalArgumentException("유효하지 않은 소정훈련시간(dailyHours)이 제공되었습니다: " + dailyHours);
    }

    int requiredHours = dailyHours;
    int halfHours = requiredHours / 2;

    return switch (status) {
      case "ATTENDANCE", "VACATION" -> requiredHours;
      case "LATE", "LEAVE_EARLY" -> halfHours;
      default -> 0;
    };
  }


}
