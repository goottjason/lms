package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.ParticipationVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 출결(participation) 서비스 인터페이스 출결 기록 생성, 입퇴실 처리, 조회 등 제공
 */
public interface AttendanceService {

  // 모든 과정의 출결 기록 생성 (스케줄러)
  void createDailyAttendanceForAllCourses(LocalDate participationDate);

  // 특정 과정의 출결 기록 생성
  int createDailyAttendanceForCourse(Integer courseId, LocalDate participationDate);

  // 입실 처리
  boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime,
      LocalDate participationDate);

  // 퇴실 처리
  boolean processCheckOut(Integer learnerEnrollmentId, LocalDateTime checkOutTime,
      LocalDate participationDate);

  // 오늘 출결 기록 + 화면 표시 상태
  ParticipationVO getTodayParticipationWithDisplayStatus(Integer learnerEnrollmentId,
      LocalDate participationDate);

  // 날짜별 출결 기록 + 화면 표시 상태
  List<ParticipationVO> getParticipationByDateWithDisplayStatus(LocalDate participationDate);

  // 퇴실 예상 상태 예측
  String predictAttendanceStatus(Integer learnerEnrollmentId, LocalDateTime predictedCheckOut,
      LocalDate participationDate);

  // 전체 휴강/공휴일 여부
  boolean isClassDay(LocalDate date);

  // 특정 과정의 휴강일 여부
  boolean isClassDayForCourse(Integer courseId, LocalDate date);

  // 진행률 계산 메서드
  double getProgressPercentage(Integer learnerEnrollmentId);

}
